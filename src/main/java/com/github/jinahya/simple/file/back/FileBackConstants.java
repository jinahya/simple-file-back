/*
 * Copyright 2014 Jin Kwon.
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


/**
 *
 * @author Jin Kwon
 */
public final class FileBackConstants {


    static final String PROPERTY_PREFIX
        = "http://www.github.com/jinahya/simple-file-back";


    public static final String PROPERTY_KEY_BYTES
        = PROPERTY_PREFIX + "/key_bytes";


    public static final String PROPERTY_SOURCE_STREAM
        = PROPERTY_PREFIX + "/source_stream";


    public static final String PROPERTY_TARGET_STREAM
        = PROPERTY_PREFIX + "/target_stream";


    public static final String PROPERTY_LOCATED_PATH
        = PROPERTY_PREFIX + "/located_path";


    public static final String PROPERTY_LOCATED_FILE
        = PROPERTY_PREFIX + "/located_file";


    public static final String PROPERTY_BYTES_COPIED
        = PROPERTY_PREFIX + "/bytes_copied";


    private FileBackConstants() {

        super();
    }


}

