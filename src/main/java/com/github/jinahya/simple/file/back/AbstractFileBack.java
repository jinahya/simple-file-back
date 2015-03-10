/*
 * Copyright 2015 Jin Kwon &lt;jinahya_at_gmail.com&gt;.
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
import java.io.OutputStream;
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import static java.util.Optional.ofNullable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public abstract class AbstractFileBack implements FileBack {


    protected static final String DEFAULT_KEY_DIGEST_ALGORITHM = "SHA-1";


    protected static final int DEFAULT_PATH_TOKEN_LENGTH = 3;


    protected static final String DEFAULT_PATH_TOKEN_DELIMITER = "/";


    static String pathName(final ByteBuffer fileKey,
                           final String digestAlgorithm, final int tokenLength,
                           final String tokenDelimiter)
        throws NoSuchAlgorithmException {

        final Logger logger = getLogger(lookup().lookupClass());

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


    static String pathName(final FileContext fileContext,
                           final boolean checkPathNameSupplier,
                           final boolean checkSourceKeySupplier,
                           final boolean checkTargetKeySupplier,
                           final String digestAlgorithm, final int tokenLength,
                           final String tokenDelimiter)
        throws FileBackException, NoSuchAlgorithmException {

        final Logger logger = getLogger(lookup().lookupClass());

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        if (checkPathNameSupplier) {
            final String[] pathName_ = new String[1];
            ofNullable(fileContext.pathNameSupplier()).ifPresent(pns -> {
                pathName_[0] = pns.get();
            });
            if (pathName_[0] != null) {
                return pathName_[0];
            }
        }

        final ByteBuffer[] fileKey_ = new ByteBuffer[1];
        if (fileKey_[0] == null && checkSourceKeySupplier) {
            ofNullable(fileContext.sourceKeySupplier()).ifPresent(sks -> {
                ofNullable(sks.get()).ifPresent(sk -> {
                    logger.debug("source key: {}", sk);
                    fileKey_[0] = sk;
                });
            });
        }
        if (fileKey_[0] == null && checkTargetKeySupplier) {
            ofNullable(fileContext.targetKeySupplier()).ifPresent(tks -> {
                ofNullable(tks.get()).ifPresent(tk -> {
                    logger.debug("target key: {}", tk);
                    fileKey_[0] = tk;
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
            .ifPresent(pnc -> pnc.accept(pathName));

        return pathName;
    }


    static long copy(final ReadableByteChannel source,
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


    static long copy(final ReadableByteChannel source,
                     final WritableByteChannel target)
        throws IOException {

        return copy(source, target, 16 * 1024);
    }


    @Override
    public void operate(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final FileOperation fileOperation
            = ofNullable(fileContext.fileOperationSupplier())
            .orElseThrow(
                () -> new FileBackException("no file operation supplier set"))
            .get();
        logger.trace("file operation: {}", fileOperation);
        if (fileOperation == null) {
            logger.error("null file operation supplied");
            return;
        }

        switch (fileOperation) {
            case COPY:
                copy(fileContext);
                break;
            case DELETE:
                delete(fileContext);
                break;
            case READ:
                read(fileContext);
                break;
            case WRITE:
                write(fileContext);
                break;
            default:
                throw new FileBackException(
                    "unsupported operation: " + fileOperation);
        }
    }


    protected abstract void copy(final FileContext fileContext)
        throws IOException, FileBackException;


    protected abstract void delete(final FileContext fileContext)
        throws IOException, FileBackException;


    protected abstract void read(final FileContext fileContext)
        throws IOException, FileBackException;


    protected abstract void write(final FileContext fileContext)
        throws IOException, FileBackException;


//    protected void writeFromSourceChannelWritableByteChannel(
//        final FileContext fileContext,
//        final Supplier<WritableByteChannel> targetChannelSupplier)
//        throws IOException, FileBackException {
//
//        ofNullable(fileContext.sourceChannelSupplier()).map(Supplier::get)
//            .ifPresent(sourceChannel -> {
//                ofNullable(targetChannelSupplier.get())
//                .ifPresent(targetChannel -> {
//                    try {
//                        final long fileCopied = FileBackUtilities.copy(
//                            sourceChannel, targetChannel);
//                        ofNullable(fileContext.sourceCopiedConsumer())
//                        .ifPresent(sourceCopiedConsumer -> {
//                            sourceCopiedConsumer.accept(fileCopied);
//                        });
//                    } catch (final IOException ioe) {
//                        logger.error("failed to copy from {} to {}",
//                                     sourceChannel, targetChannel);
//                    }
//                });
//            });
//    }
//    protected void writeFromSourceChannelToOutputStream(
//        final FileContext fileContext,
//        final Supplier<OutputStream> targetStreamSupplier)
//        throws IOException, FileBackException {
//
//        writeFromSourceChannelWritableByteChannel(fileContext, () -> {
//            return ofNullable(targetStreamSupplier.get())
//                .map(Channels::newChannel)
//                .orElse(null);
//        });
//    }
    public String getKeyDigestAlgorithm() {

        return keyDigestAlgorithm;
    }


    public void setKeyDigestAlgorithm(final String keyDigestAlgorithm) {

        this.keyDigestAlgorithm = keyDigestAlgorithm;
    }


    public int getPathTokeyLength() {

        return pathTokeyLength;
    }


    public void setPathTokeyLength(final int pathTokeyLength) {

        this.pathTokeyLength = pathTokeyLength;
    }


    public String getPathTokenDelimiter() {

        return pathTokenDelimiter;
    }


    public void setPathTokenDelimiter(final String pathTokenDelimiter) {

        this.pathTokenDelimiter = pathTokenDelimiter;
    }


    protected String pathName(final FileContext fileContext,
                              final boolean checkPathNameSupplier,
                              final boolean checkSourceKeySupplier,
                              final boolean checkTargetKeySupplier)
        throws FileBackException {

        try {
            return pathName(fileContext, checkPathNameSupplier,
                            checkSourceKeySupplier, checkTargetKeySupplier,
                            getKeyDigestAlgorithm(), getPathTokeyLength(),
                            getPathTokenDelimiter());
        } catch (final NoSuchAlgorithmException nsae) {
            throw new FileBackException(nsae);
        }
    }


    private transient final Logger logger = getLogger(lookup().lookupClass());


    private String keyDigestAlgorithm = DEFAULT_KEY_DIGEST_ALGORITHM;


    private int pathTokeyLength = DEFAULT_PATH_TOKEN_LENGTH;


    private String pathTokenDelimiter = DEFAULT_PATH_TOKEN_DELIMITER;


}

