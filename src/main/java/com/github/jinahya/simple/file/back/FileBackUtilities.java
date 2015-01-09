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


import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public final class FileBackUtilities {


    public static String keyBufferToPathName(final ByteBuffer keyBuffer,
                                             final String digestAlgorithm,
                                             final int tokenLength,
                                             final String tokenDelimiter)
        throws NoSuchAlgorithmException {

        if (keyBuffer == null) {
            throw new NullPointerException("null keyBuffer");
        }

        if (keyBuffer.remaining() == 0) {
            throw new IllegalArgumentException("keyBuffer.remaining == 0");
        }

        if (digestAlgorithm == null) {
            throw new NullPointerException("null digestAlgorithm");
        }

        if (tokenLength < 0) {
            throw new IllegalArgumentException(
                "tokenLength(" + tokenLength + ") < 0");
        }

        if (tokenDelimiter == null) {
            throw new NullPointerException("null tokenDelimiter");
        }

        final MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
        digest.update(keyBuffer.asReadOnlyBuffer());
        final byte[] digested = digest.digest();

        final String hexed = IntStream.range(0, digested.length)
            .collect(() -> new StringBuilder(digested.length * 2),
                     (b, i) -> new Formatter(b).format(
                         "%02x", digested[i] & 0xFF),
                     StringBuilder::append)
            .toString();

        final String joined = Stream
            .of(hexed.split("(?<=\\G.{" + tokenLength + "})"))
            .collect(Collectors.joining(tokenDelimiter));

        return joined;
    }


    private FileBackUtilities() {

        super();
    }


}

