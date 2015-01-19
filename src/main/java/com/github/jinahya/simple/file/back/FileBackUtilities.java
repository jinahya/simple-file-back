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
 * A utility class for file back implementations.
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public final class FileBackUtilities {


    /**
     * Converts given file key to a path name.
     *
     * @param fileKey the file key to convert
     * @param digestAlgorithm a message digest algorithm to hash.
     * @param tokenLength the number of characters to split.
     * @param tokenDelimiter the delimiter string used when joining split
     * tokens.
     *
     * @return a path name.
     *
     * @throws NoSuchAlgorithmException if {@code digestAlgorithm} is unknown.
     */
    public static String fileKeyToPathName(final ByteBuffer fileKey,
                                           final String digestAlgorithm,
                                           final int tokenLength,
                                           final String tokenDelimiter)
        throws NoSuchAlgorithmException {

        if (fileKey == null) {
            throw new NullPointerException("null fileKey");
        }

        if (fileKey.remaining() == 0) {
            throw new IllegalArgumentException("keyBuffer.remaining == 0");
        }

        if (digestAlgorithm == null) {
            throw new NullPointerException("null digestAlgorithm");
        }

        if (tokenLength <= 0) {
            throw new IllegalArgumentException(
                "tokenLength(" + tokenLength + ") <= 0");
        }

        if (tokenDelimiter == null) {
            throw new NullPointerException("null tokenDelimiter");
        }

        final MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
        digest.update(fileKey.asReadOnlyBuffer());
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

