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


    public static final String PROPERTY_KEY_BUFFER_SUPPLIER
        = PROPERTY_PREFIX + "/key_buffer_supplier";


    public static final String PROPERTY_SOURCE_CHANNEL_SUPPLIER
        = PROPERTY_PREFIX + "/source_channel_supplier";


    public static final String PROPERTY_TARGET_CHANNEL_SUPPLIER
        = PROPERTY_PREFIX + "/target_channel_supplier";


    public static final String PROPERTY_PATH_NAME_CONSUMER
        = PROPERTY_PREFIX + "/path_name_consumer";


    public static final String PROPERTY_LOCAL_PATH_CONSUMER
        = PROPERTY_PREFIX + "/local_path_consumer";


    public static final String PROPERTY_BYTES_COPIED_CONSUMER
        = PROPERTY_PREFIX + "/bytes_copied_consumer";


    /**
     * An identifier function converting digested bytes to a base64url string.
     */
    @Deprecated
    public static final Function<byte[], String> IDENTIFIER_ENCODER_BASE64URL
        = d -> Base64.getUrlEncoder().withoutPadding().encodeToString(d);


    /**
     * An identifier function converting digested bytes into a hex string.
     */
    public static final Function<byte[], String> IDENTIFIER_ENCODER_HEX
        = d -> IntStream.range(0, d.length * 2)
        .map(i -> (d[i / 2] >> ((i & 1) == 0 ? 4 : 0)) & 0x0F)
        .mapToObj(Integer::toHexString)
        .collect(joining());


//    public static final Function<String, byte[]> IDENTIFIER_DECODER_HEX
//        = s -> IntStream.range(0, s.length() / 2)
//        .map(i -> Byte.parseByte(s.substring(i * 2, i * 2 + 2), 0x10))
//        .collect(joining());


    private FileBackConstants() {

        super();
    }


}

