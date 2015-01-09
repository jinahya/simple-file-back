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


import com.github.jinahya.simple.file.back.FileContext.PropertyKey;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public abstract class AbstractFileContext implements FileContext {


    @Override
    public Optional<Object> property(final PropertyKey key) {

        if (key == null) {
            throw new NullPointerException("null key");
        }

        return Optional.ofNullable(properties().get(key));
    }


    @Override
    public Optional<Object> property(final PropertyKey key,
                                     final Object value) {

        if (key == null) {
            throw new NullPointerException("null key");
        }

        if (value == null) {
            return Optional.ofNullable(properties().remove(key));
        }

        return Optional.ofNullable(properties().put(key, value));
    }


    protected Map<PropertyKey, Object> properties() {

        if (properties == null) {
            properties = new EnumMap<>(PropertyKey.class);
        }

        return properties;
    }


    private Map<PropertyKey, Object> properties;


}

