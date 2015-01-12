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


import com.google.common.io.Files;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;
import static java.util.concurrent.ThreadLocalRandom.current;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public final class FileBackTests {


    /**
     * Generates a new random root path.
     *
     * @return a new random root path.
     */
    public static Path randomLocalRoot() {

        final File tempDir = Files.createTempDir();
        tempDir.deleteOnExit();

        return tempDir.toPath();
    }


    public static ByteBuffer randomKeyBuffer() {

        final byte[] keyBytes = new byte[current().nextInt(1, 128)];
        current().nextBytes(keyBytes);

        return ByteBuffer.wrap(keyBytes);
    }


    public static InputStream randomSourceStream() {

        final byte[] sourceByts = new byte[current().nextInt(0, 1024)];
        current().nextBytes(sourceByts);

        return new ByteArrayInputStream(sourceByts);
    }


    public static ReadableByteChannel randomSourceChannel() {

        return Channels.newChannel(randomSourceStream());
    }


    public static OutputStream randomTargetStream() {

        return new ByteArrayOutputStream(1024);
    }


    public static WritableByteChannel randomTargetChannel() {

        return Channels.newChannel(randomTargetStream());
    }


    private FileBackTests() {

        super();
    }


}

