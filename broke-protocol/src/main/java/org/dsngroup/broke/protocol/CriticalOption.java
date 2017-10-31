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

package org.dsngroup.broke.protocol;

/**
 * Records the critical-option field of message.
 */
public enum CriticalOption {
    BEST_EFFORT(0),
    CRITICAL(1);
    public final int value;
    CriticalOption(final int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
