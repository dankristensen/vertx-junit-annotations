/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vertx.java.test.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;

/**
 * @author swilliams
 * 
 */
public class DeploymentHandler implements Handler<AsyncResult<String>> {

	private final CountDownLatch latch;

	private final Set<String> deploymentIDs = new HashSet<>();

	public DeploymentHandler(final CountDownLatch latch) {
		this.latch = latch;
	}

	public String getDeploymentID() {
		if (deploymentIDs.size() == 0) {
			return null;
		}
		return deploymentIDs.iterator().next();
	}

	@Override
	public void handle(AsyncResult<String> event) {
		if (event.succeeded()) {
			if (event.result() != null) {
				deploymentIDs.add(event.result());
			}
		} else {
			throw new RuntimeException("Unable to deploy module/verticle",
					event.cause());
		}
		latch.countDown();
	}

}