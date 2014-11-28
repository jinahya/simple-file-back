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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import static java.util.concurrent.ThreadLocalRandom.current;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public final class FileBackTests {


    public static byte[] randomKeyBytes() {

        final byte[] keyBytes = new byte[current().nextInt(1, 128)];

        current().nextBytes(keyBytes);

        return keyBytes;
    }


    public static InputStream randomSourceStream(final byte[] sourceBytes) {

        return new ByteArrayInputStream(sourceBytes);
    }


    public static InputStream randomSourceStream() {

        final byte[] sourceByts = new byte[current().nextInt(0, 1024)];
        current().nextBytes(sourceByts);

        return randomSourceStream(sourceByts);
    }


    public static OutputStream randomTargetStream() {

        return new ByteArrayOutputStream(1024);
    }




    private FileBackTests() {

        super();
    }


}

