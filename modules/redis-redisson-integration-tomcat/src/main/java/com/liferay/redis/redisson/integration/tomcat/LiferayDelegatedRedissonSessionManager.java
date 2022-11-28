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

import com.liferay.redis.redisson.integration.tomcat.helper.RedissonSessionManagerHelper;

import java.util.concurrent.CountDownLatch;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.tomcat.RedissonSessionManager;

/**
 * @author Mariano Álvaro Sáiz
 */
public class LiferayDelegatedRedissonSessionManager
	extends RedissonSessionManager {

	public static void setRedissonSessionManagerHelper(
		RedissonSessionManagerHelper redissonSessionManagerHelper) {

		_redissonSessionManagerHelper = redissonSessionManagerHelper;

		_countDownLatch.countDown();
	}

	public Session createEmptySession() {
		return _redissonSessionManagerHelper.createEmptySession(
			this, RedissonSessionManager.ReadMode.valueOf(getReadMode()),
			RedissonSessionManager.UpdateMode.valueOf(getUpdateMode()),
			isBroadcastSessionEvents(), isBroadcastSessionUpdates());
	}

	public Session createSession(String sessionId) {
		return super.createSession(sessionId);
	}

	protected RedissonClient buildClient() throws LifecycleException {
		_waitForRedissonSessionManagerHelper();

		RedissonClient redissonClient = super.buildClient();

		Config config = redissonClient.getConfig();

		Class<?> clazz = getClass();

		config.setCodec(
			_redissonSessionManagerHelper.getCodec(clazz.getClassLoader()));

		return redissonClient;
	}

	private void _waitForRedissonSessionManagerHelper() {
		try {
			_countDownLatch.await();
		}
		catch (InterruptedException interruptedException) {
			throw new IllegalStateException(interruptedException);
		}
	}

	private static CountDownLatch _countDownLatch = new CountDownLatch(1);
	private static RedissonSessionManagerHelper _redissonSessionManagerHelper;

}