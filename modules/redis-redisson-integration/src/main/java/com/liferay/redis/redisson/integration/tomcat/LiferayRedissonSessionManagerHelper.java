/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.redis.redisson.integration.tomcat;

import com.liferay.portal.kernel.io.Deserializer;
import com.liferay.portal.kernel.io.Serializer;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.redis.redisson.integration.codec.LiferayFstCodec;
import com.liferay.redis.redisson.integration.tomcat.helper.RedissonSessionManagerHelper;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import java.nio.ByteBuffer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.catalina.Session;

import org.redisson.codec.FstCodec;
import org.redisson.tomcat.RedissonSession;
import org.redisson.tomcat.RedissonSessionManager;

/**
 * @author Mariano Álvaro Sáiz
 */
public class LiferayRedissonSessionManagerHelper
	implements RedissonSessionManagerHelper {

	@Override
	public Session createEmptySession(
		RedissonSessionManager manager,
		RedissonSessionManager.ReadMode readMode,
		RedissonSessionManager.UpdateMode updateMode,
		boolean broadcastSessionEvents, boolean broadcastSessionUpdates) {

		return new SerializableRedissonSession(
			manager, readMode, updateMode, broadcastSessionEvents,
			broadcastSessionUpdates);
	}

	@Override
	public FstCodec getCodec(ClassLoader classLoader) {
		return new LiferayFstCodec(classLoader);
	}

	private static final Set<Class<?>> _safeClasses = new HashSet<>(
		Arrays.asList(
			Boolean.class, Byte.class, Character.class, Class.class,
			Double.class, Float.class, Integer.class, Long.class, Short.class,
			String.class));

	private static class LazySerializable implements Serializable {

		public byte[] getData() {
			return _data;
		}

		public Serializable getSerializable() {
			Deserializer deserializer = new Deserializer(
				ByteBuffer.wrap(_data));

			try {
				return deserializer.readObject();
			}
			catch (ClassNotFoundException classNotFoundException) {
				_log.error(
					"Unable to deserialize object", classNotFoundException);

				return null;
			}
		}

		private LazySerializable(byte[] data) {
			_data = data;
		}

		private static final Log _log = LogFactoryUtil.getLog(
			LazySerializable.class);

		private final byte[] _data;

	}

	private static class LazySerializableObjectWrapper
		implements Externalizable {

		public LazySerializableObjectWrapper() {
		}

		public Serializable getSerializable() {
			if (_serializable instanceof LazySerializable) {
				LazySerializable lazySerializable =
					(LazySerializable)_serializable;

				Serializable serializable = lazySerializable.getSerializable();

				if (serializable == null) {
					return null;
				}

				_serializable = serializable;
			}

			return _serializable;
		}

		public void readExternal(ObjectInput objectInput) throws IOException {
			byte[] data = new byte[objectInput.readInt()];

			objectInput.readFully(data);

			_serializable = new LazySerializable(data);
		}

		public void writeExternal(ObjectOutput objectOutput)
			throws IOException {

			byte[] data = _getData();

			objectOutput.writeInt(data.length);

			objectOutput.write(data, 0, data.length);
		}

		private LazySerializableObjectWrapper(Serializable serializable) {
			_serializable = serializable;
		}

		private byte[] _getData() {
			if (_serializable instanceof LazySerializable) {
				LazySerializable lazySerializable =
					(LazySerializable)_serializable;

				return lazySerializable.getData();
			}

			Serializer serializer = new Serializer();

			serializer.writeObject(_serializable);

			ByteBuffer byteBuffer = serializer.toByteBuffer();

			return byteBuffer.array();
		}

		private volatile Serializable _serializable;

	}

	private static class SerializableRedissonSession extends RedissonSession {

		public Object getAttribute(String name) {
			Object value = super.getAttribute(name);

			if (value instanceof LazySerializableObjectWrapper) {
				LazySerializableObjectWrapper lazySerializableObjectWrapper =
					(LazySerializableObjectWrapper)value;

				return lazySerializableObjectWrapper.getSerializable();
			}

			return value;
		}

		public void setAttribute(String name, Object value) {
			if (!(value instanceof Serializable)) {
				super.setAttribute(name, value);

				return;
			}

			Class<?> clazz = value.getClass();

			if (!_safeClasses.contains(clazz)) {
				value = new LazySerializableObjectWrapper((Serializable)value);
			}

			super.setAttribute(name, value);
		}

		private SerializableRedissonSession(
			RedissonSessionManager manager,
			RedissonSessionManager.ReadMode readMode,
			RedissonSessionManager.UpdateMode updateMode,
			boolean broadcastSessionEvents, boolean broadcastSessionUpdates) {

			super(
				manager, readMode, updateMode, broadcastSessionEvents,
				broadcastSessionUpdates);
		}

	}

}