/*
 * Copyright 2014 Jin Kwon &lt;jinahya_at_gmail.com&gt;.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jinahya.simple.file.back;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class DefaultFileContext implements FileContext {


    @Override
    public Optional<Object> property(final String name) {

        if (name == null) {
            throw new NullPointerException("null name");
        }

        return Optional.ofNullable(properties().get(name));
    }


    @Override
    public Optional<Object> property(final String name, final Object value) {

        if (name == null) {
            throw new NullPointerException("null name");
        }

        if (value == null) {
            return Optional.ofNullable(properties().remove(name));
        }

        return Optional.ofNullable(properties().put(name, value));
    }


    protected Map<String, Object> properties() {

        if (properties == null) {
            properties = new HashMap<>();
        }

        return properties;
    }


    private Map<String, Object> properties;


}

