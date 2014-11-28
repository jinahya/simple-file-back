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


import java.util.Base64;
import java.util.function.Function;
import static java.util.stream.Collectors.joining;
import java.util.stream.IntStream;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public final class FileBackConstants {


    static final String PROPERTY_PREFIX
        = "http://www.github.com/jinahya/simple-file-back";


    public static final String PROPERTY_KEY_BYTES
        = PROPERTY_PREFIX + "/key_bytes";


    public static final String PROPERTY_SOURCE_STREAM
        = PROPERTY_PREFIX + "/source_stream";


    public static final String PROPERTY_SOURCE_CHANNEL
        = PROPERTY_PREFIX + "/source_channel";


    public static final String PROPERTY_TARGET_STREAM
        = PROPERTY_PREFIX + "/target_stream";


    public static final String PROPERTY_TARGET_CHANNEL
        = PROPERTY_PREFIX + "/target_channel";


    public static final String PROPERTY_PATH_NAME
        = PROPERTY_PREFIX + "/path_name";


    public static final String PROPERTY_PATH_EXIST
        = PROPERTY_PREFIX + "/path_exist";


    public static final String PROPERTY_LOCATED_PATH
        = PROPERTY_PREFIX + "/located_path";


    public static final String PROPERTY_FILE_EXIST
        = PROPERTY_PREFIX + "/file_exist";


    public static final String PROPERTY_LOCATED_FILE
        = PROPERTY_PREFIX + "/located_file";


    public static final String PROPERTY_BYTES_COPIED
        = PROPERTY_PREFIX + "/bytes_copied";


    public static final Function<byte[], String> IDENTIFIER_BASE64URL
        = d -> Base64.getUrlEncoder().withoutPadding().encodeToString(d);


    public static final Function<byte[], String> IDENTIFIER_HEX
        = d -> IntStream.range(0, d.length * 2)
        .map(i -> (d[i / 2] >> ((i & 1) == 0 ? 4 : 0)) & 0x0F)
        .mapToObj(Integer::toHexString)
        .collect(joining());


    private FileBackConstants() {

        super();
    }


}

