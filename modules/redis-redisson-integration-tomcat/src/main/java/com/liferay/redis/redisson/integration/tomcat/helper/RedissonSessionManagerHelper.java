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

package com.liferay.redis.redisson.integration.tomcat.helper;

import org.apache.catalina.Session;

import org.redisson.codec.FstCodec;
import org.redisson.tomcat.RedissonSessionManager;

/**
 * @author Mariano Álvaro Sáiz
 */
public interface RedissonSessionManagerHelper {

	public Session createEmptySession(
		RedissonSessionManager manager,
		RedissonSessionManager.ReadMode readMode,
		RedissonSessionManager.UpdateMode updateMode,
		boolean broadcastSessionEvents, boolean broadcastSessionUpdates);

	public FstCodec getCodec(ClassLoader classLoader);

}