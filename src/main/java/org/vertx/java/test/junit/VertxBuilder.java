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
package org.vertx.java.test.junit;

import org.vertx.java.core.impl.DefaultVertx;

/**
 * @author swilliams
 *
 */
public class VertxBuilder {

  private String address;

  private int port;

  public VertxBuilder setAddress(String address) {
    this.address = address;
    return this;
  }

  public VertxBuilder setPort(int port) {
    this.port = port;
    return this;
  }

  public DefaultVertx build() {
    try {
      DefaultVertx vertx;
      if (port > -1 && address != null) {
        vertx = new DefaultVertx(port, address);
      }
      else if (address != null) {
        vertx = new DefaultVertx(address);
      }
      else {
        vertx = new DefaultVertx();
      }
      return vertx;
    }
    finally {
      this.address = null;
      this.port = -1;
    }
  }

}
