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
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public final class FileBackConstants {


    static final String PROPERTY_PREFIX
        = "http://www.github.com/jinahya/simple-file-back";


    public static final String PROPERTY_FILE_BACK_SUPPLIER
        = PROPERTY_PREFIX + "/file_back_supplier";


    public static final String PROPERTY_KEY_BUFFER_SUPPLIER
        = PROPERTY_PREFIX + "/key_buffer_supplier";


    public static final String PROPERTY_FILE_SUFFIX_SUPPLIER
        = PROPERTY_PREFIX + "/file_suffix_supplier";


    public static final String PROPERTY_CONTENT_TYPE_CONSUMER
        = PROPERTY_PREFIX + "/content_type_consumer";


    public static final String PROPERTY_CONTENT_LENGTH_CONSUMER
        = PROPERTY_PREFIX + "/content_length_consumer";


    public static final String PROPERTY_CONTENT_TYPE_SUPPLIER
        = PROPERTY_PREFIX + "/content_type_suppplier";


    public static final String PROPERTY_CONTENT_LENGTH_SUPPLIER
        = PROPERTY_PREFIX + "/content_length_suppplier";


    public static final String PROPERTY_SOURCE_CHANNEL_SUPPLIER
        = PROPERTY_PREFIX + "/source_channel_supplier";


    public static final String PROPERTY_SOURCE_COPIED_CONSUMER
        = PROPERTY_PREFIX + "/source_copied_consumer";


    public static final String PROPERTY_TARGET_CHANNEL_SUPPLIER
        = PROPERTY_PREFIX + "/target_channel_supplier";


    public static final String PROPERTY_TARGET_COPIED_CONSUMER
        = PROPERTY_PREFIX + "/target_copied_consumer";


    public static final String PROPERTY_PATH_NAME_CONSUMER
        = PROPERTY_PREFIX + "/path_name_consumer";


    public static final String PROPERTY_PATH_NAME_SUPPLIER
        = PROPERTY_PREFIX + "/path_name_supplier";


    public static final String PROPERTY_FILE_DELETED_CONSUMER
        = PROPERTY_PREFIX + "/file_deleted_consumer";


    private FileBackConstants() {

        super();
    }


}

