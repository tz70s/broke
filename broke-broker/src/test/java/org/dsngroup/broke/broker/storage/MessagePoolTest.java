/*
 * Copyright (c) 2017 original authors and authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dsngroup.broke.broker.storage;

import org.dsngroup.broke.broker.ServerContext;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessagePoolTest {

    @Test
    public void testTopicPutAndGet() {
        ServerContext serverContext = new ServerContext();

        MessagePool messagePool = serverContext.getMessagePool();
        messagePool.putContentOnTopic("foo", "bar");
        assertEquals("bar", messagePool.getContentFromTopic("foo"));
    }
}