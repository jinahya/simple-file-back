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


import java.io.IOException;
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import static java.util.Optional.ofNullable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


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
    public static String pathName(final ByteBuffer fileKey,
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


    /**
     *
     * @param fileContext
     * @param checkPathNameSupplier
     * @param checkSourceKeySupplier
     * @param checkTargetKeySupplier
     * @param digestAlgorithm
     * @param tokenLength
     * @param tokenDelimiter
     *
     * @return
     *
     * @throws FileBackException
     * @throws NoSuchAlgorithmException
     *
     * @see #pathName(java.nio.ByteBuffer, java.lang.String, int,
     * java.lang.String)
     */
    public static String pathName(final FileContext fileContext,
                                  final boolean checkPathNameSupplier,
                                  final boolean checkSourceKeySupplier,
                                  final boolean checkTargetKeySupplier,
                                  final String digestAlgorithm,
                                  final int tokenLength,
                                  final String tokenDelimiter)
        throws FileBackException, NoSuchAlgorithmException {

        final Logger logger = getLogger(lookup().lookupClass());

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        if (checkPathNameSupplier) {
            final String[] pathName_ = new String[1];
            ofNullable(fileContext.pathNameSupplier())
                .ifPresent(pathNameSupplier -> {
                    pathName_[0] = pathNameSupplier.get();
                });
            if (pathName_[0] != null) {
                return pathName_[0];
            }
        }

        final ByteBuffer[] fileKey_ = new ByteBuffer[1];
        if (fileKey_[0] == null && checkSourceKeySupplier) {
            ofNullable(fileContext.sourceKeySupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(k -> {
                    logger.debug("source key: {}", k);
                    fileKey_[0] = k;
                });
            });
        }
        if (fileKey_[0] == null && checkTargetKeySupplier) {
            ofNullable(fileContext.targetKeySupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(k -> {
                    logger.debug("target key: {}", k);
                    fileKey_[0] = k;
                });
            });
        }

        final ByteBuffer fileKey = fileKey_[0];
        if (fileKey == null) {
            throw new FileBackException("no file key located");
        }

        final String pathName = pathName(
            fileKey, digestAlgorithm, tokenLength, tokenDelimiter);

        ofNullable(fileContext.pathNameConsumer())
            .ifPresent(c -> c.accept(pathName));

        return pathName;
    }


    public static long copy(final ReadableByteChannel source,
                            final WritableByteChannel target,
                            final int capacity)
        throws IOException {

        long count = 0L;

        final ByteBuffer buffer = ByteBuffer.allocate(capacity);

        for (int read = -1; (read = source.read(buffer)) != -1; count += read) {
            buffer.flip();
            target.write(buffer);
            buffer.compact();
        }

        count += buffer.remaining();
        for (buffer.flip(); buffer.hasRemaining(); target.write(buffer));

        return count;
    }


    public static long copy(final ReadableByteChannel source,
                            final WritableByteChannel target)
        throws IOException {

        return copy(source, target, 16 * 1024);
    }


    private FileBackUtilities() {

        super();
    }


}

