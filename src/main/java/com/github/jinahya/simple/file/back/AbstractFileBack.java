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
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Objects;
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
abstract class AbstractFileBack implements FileBack {


    protected static final String DEFAULT_KEY_DIGEST_ALGORITHM = "SHA-1";


    protected static final int DEFAULT_PATH_TOKEN_LENGTH = 3;


    protected static final String DEFAULT_PATH_TOKEN_DELIMITER = "/";


    static String filePath(final ByteBuffer fileKey,
                           final String digestAlgorithm, final int tokenLength,
                           final String tokenDelimiter)
        throws NoSuchAlgorithmException {

        final Logger logger = getLogger(lookup().lookupClass());

        Objects.requireNonNull(fileKey, "null fileKey");
        if (fileKey.remaining() == 0) {
            throw new IllegalArgumentException("keyBuffer.remaining == 0");
        }

        Objects.requireNonNull(digestAlgorithm, "null digestAlgorithm");

        if (tokenLength <= 0) {
            throw new IllegalArgumentException(
                "tokenLength(" + tokenLength + ") <= 0");
        }

        Objects.requireNonNull(tokenDelimiter, "null tokenDelimiter");

        final MessageDigest digest = MessageDigest.getInstance(digestAlgorithm);
        digest.update(fileKey.asReadOnlyBuffer());
        final byte[] digested = digest.digest();

        final String hexed
            = IntStream.range(0, digested.length)
            .collect(() -> new StringBuilder(digested.length * 2),
                     (b, i) -> new Formatter(b).format(
                         "%02x", digested[i] & 0xFF),
                     StringBuilder::append)
            .toString();

        final String joined
            = Stream
            .of(hexed.split("(?<=\\G.{" + tokenLength + "})"))
            .collect(Collectors.joining(tokenDelimiter));

        return joined;
    }


    /**
     *
     * @param fileContext
     * @param checkSourcePathSupplier
     * @param checkTargetPathSupplier
     * @param checkSourceKeySupplier
     * @param checkTargetKeySupplier
     * @param digestAlgorithm
     * @param tokenLength
     * @param tokenDelimiter
     *
     * @return the located file path.
     *
     * @throws FileBackException if no file path located
     * @throws NoSuchAlgorithmException if {@code digestAlgorithm} is unknown.
     */
    static String filePath(final FileContext fileContext,
                           final boolean checkSourcePathSupplier,
                           final boolean checkTargetPathSupplier,
                           final boolean checkSourceKeySupplier,
                           final boolean checkTargetKeySupplier,
                           final String digestAlgorithm, final int tokenLength,
                           final String tokenDelimiter)
        throws FileBackException, NoSuchAlgorithmException {

        final Logger logger = getLogger(lookup().lookupClass());

        Objects.requireNonNull(fileContext, "null fileContext");

        if (checkSourcePathSupplier) {
            final String[] sourcePath_ = new String[1];
            ofNullable(fileContext.sourcePathSupplier()).ifPresent(sps -> {
                sourcePath_[0] = sps.get();
            });
            final String sourcePath = sourcePath_[0];
            if (sourcePath != null) {
                return sourcePath;
            }
        }

        if (checkTargetPathSupplier) {
            final String[] targetPath_ = new String[1];
            ofNullable(fileContext.targetPathSupplier()).ifPresent(tss -> {
                targetPath_[0] = tss.get();
            });
            final String targetPath = targetPath_[0];
            if (targetPath != null) {
                return targetPath;
            }
        }

        if (checkSourceKeySupplier) {
            final ByteBuffer[] sourceKey_ = new ByteBuffer[1];
            ofNullable(fileContext.sourceKeySupplier()).map(Supplier::get)
                .ifPresent(sk -> {
                    logger.debug("source key: {}", sk);
                    sourceKey_[0] = sk;
                });
//            ofNullable(fileContext.sourceKeySupplier()).ifPresent(sks -> {
//                ofNullable(sks.get()).ifPresent(sk -> {
//                    logger.debug("source key: {}", sk);
//                    sourceKey_[0] = sk;
//                });
//            });
            final ByteBuffer sourceKey = sourceKey_[0];
            final String sourcePath = AbstractFileBack.filePath(
                sourceKey, digestAlgorithm, tokenLength, tokenDelimiter);
            ofNullable(fileContext.sourcePathConsumer())
                .ifPresent(spc -> spc.accept(sourcePath));
            return sourcePath;
        }

        if (checkTargetKeySupplier) {
            final ByteBuffer[] targetKey_ = new ByteBuffer[1];
            ofNullable(fileContext.targetKeySupplier()).map(Supplier::get)
                .ifPresent(tk -> {
                    logger.debug("target key: {}", tk);
                    targetKey_[0] = tk;
                });
//            ofNullable(fileContext.targetKeySupplier()).ifPresent(tks -> {
//                ofNullable(tks.get()).ifPresent(tk -> {
//                    logger.debug("target key: {}", tk);
//                    targetKey_[0] = tk;
//                });
//            });
            final ByteBuffer targetKey = targetKey_[0];
            final String targetPath = AbstractFileBack.filePath(
                targetKey, digestAlgorithm, tokenLength, tokenDelimiter);
            ofNullable(fileContext.targetPathConsumer())
                .ifPresent(tpc -> tpc.accept(targetPath));
            return targetPath;
        }

        throw new FileBackException("no file path located");
    }


    /**
     * Return the {@code pathName}.
     *
     * @param fileContext
     * @param checkPathNameSupplier
     * @param checkSourceKeySupplier
     * @param checkTargetKeySupplier
     * @param digestAlgorithm
     * @param tokenLength
     * @param tokenDelimiter
     *
     * @return the path name.
     *
     * @throws FileBackException if no path name found.
     * @throws NoSuchAlgorithmException
     * @deprecated
     */
    @Deprecated
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
            final String pathName = pathName_[0];
            if (pathName != null) {
                return pathName;
            }
        }

        if (checkSourceKeySupplier) {
            final ByteBuffer[] sourceKey_ = new ByteBuffer[1];
            ofNullable(fileContext.sourceKeySupplier()).ifPresent(sks -> {
                ofNullable(sks.get()).ifPresent(sk -> {
                    logger.debug("source key: {}", sk);
                    sourceKey_[0] = sk;
                });
            });
            final ByteBuffer sourceKey = sourceKey_[0];
            final String pathName = AbstractFileBack.filePath(
                sourceKey, digestAlgorithm, tokenLength, tokenDelimiter);
            return pathName;
        }

        if (checkTargetKeySupplier) {
            final ByteBuffer[] targetKey_ = new ByteBuffer[1];
            ofNullable(fileContext.targetKeySupplier()).ifPresent(tks -> {
                ofNullable(tks.get()).ifPresent(tk -> {
                    logger.debug("target key: {}", tk);
                    targetKey_[0] = tk;
                });
            });
            final ByteBuffer targetKey = targetKey_[0];
            final String pathName = AbstractFileBack.filePath(
                targetKey, digestAlgorithm, tokenLength, tokenDelimiter);
            return pathName;
        }

        throw new FileBackException("no path name located");
    }


    /**
     * Copies bytes from a channel to another channel using a byte buffer of
     * specified capacity.
     *
     * @param source the source channel
     * @param target the target channel
     * @param capacity the capacity of buffer.
     *
     * @return the number of bytes copied.
     *
     * @throws IOException if an I/O error occurs.
     */
    static long copy(final ReadableByteChannel source,
                     final WritableByteChannel target,
                     final int capacity)
        throws IOException {

        long count = 0L;

        final ByteBuffer buffer = ByteBuffer.allocate(capacity);
        for (int read; (read = source.read(buffer)) != -1; count += read) {
            buffer.flip();
            target.write(buffer);
            buffer.compact();
        }
        for (buffer.flip(); buffer.hasRemaining();
             count += target.write(buffer));

        return count;
    }


    /**
     * Copies bytes from {@code source} to {@code target}.
     *
     * @param source the source channel
     * @param target the target channel
     *
     * @return the number of bytes transferred.
     *
     * @throws IOException if an I/O error occurs.
     */
    static long copy(final ReadableByteChannel source,
                     final WritableByteChannel target)
        throws IOException {

        return copy(source, target, 16 * 1024);
    }


    static long copy(final InputStream source, final OutputStream target,
                     final int length)
        throws IOException {

        long count = 0L;

        final byte[] buffer = new byte[length];
        for (int read; (read = source.read(buffer)) != -1; count += read) {
            target.write(buffer, 0, read);
        }

        return count;
    }


    static long copy(final InputStream source, final OutputStream target)
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


    /**
     *
     * @param fileContext
     * @param checkPathNameSupplier
     * @param checkSourceKeySupplier
     * @param checkTargetKeySupplier
     *
     * @return
     *
     * @throws FileBackException
     */
    @Deprecated
    String filePath(final FileContext fileContext,
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


    /**
     * Finds the file path.
     *
     * @param fileContext file context
     * @param checkSourcePathSupplier the flag for checking source path.
     * @param checkTargetPathSupplier the flag for checking target path.
     * @param checkSourceKeySupplier the flag for checking source key.
     * @param checkTargetKeySupplier the flag for checking target key.
     *
     * @return found file path
     *
     * @throws FileBackException if unable to find any or
     * {@link #getKeyDigestAlgorithm()} is unknown.
     */
    String filePath(final FileContext fileContext,
                    final boolean checkSourcePathSupplier,
                    final boolean checkTargetPathSupplier,
                    final boolean checkSourceKeySupplier,
                    final boolean checkTargetKeySupplier)
        throws FileBackException {

        try {
            return AbstractFileBack.this.filePath(fileContext, checkSourcePathSupplier,
                                                  checkTargetPathSupplier, checkSourceKeySupplier,
                                                  checkTargetKeySupplier, getKeyDigestAlgorithm(),
                                                  getPathTokeyLength(), getPathTokenDelimiter());
        } catch (final NoSuchAlgorithmException nsae) {
            throw new FileBackException(nsae);
        }
    }


    /**
     * Operates the {@link FileOperation#COPY} using specified temp file.
     *
     * @param fileContext the file context
     * @param tempFile the temporary file
     *
     * @throws IOException if an I/O error occurs.
     * @throws FileBackException if unable to operate.
     */
    void copyUsing(final FileContext fileContext, final Path tempFile)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        Objects.requireNonNull(tempFile, "null tempFile");

        final String sourcePath
            = AbstractFileBack.this.filePath(fileContext, true, false, true, false);
        logger.debug("source path: {}", sourcePath);

        final String targetPath
            = AbstractFileBack.this.filePath(fileContext, false, true, false, true);
        logger.debug("target path: {}", targetPath);

        if (targetPath.equals(sourcePath)) {
            throw new FileBackException(
                "target path(" + targetPath
                + ") is equals to source path(" + sourcePath + ")");
        }

        final Long[] sourceCopied_ = new Long[1];
        final FileContext sourceContext = new DefaultFileContext();
        sourceContext.fileOperationSupplier(() -> FileOperation.READ);
        sourceContext.pathNameSupplier(() -> sourcePath);
        sourceContext.sourcePathSupplier(() -> sourcePath);
        sourceContext.sourceChannelConsumer(sc -> {
            try {
                final long sourceCopied = Files.copy(
                    Channels.newInputStream(sc), tempFile,
                    StandardCopyOption.REPLACE_EXISTING);
                logger.debug("source copied: {}", sourceCopied);
                sourceCopied_[0] = sourceCopied;
            } catch (final IOException ioe) {
                logger.error("failed; source channel -> temp file", ioe);
            }
        });
        if (sourceCopied_[0] != null) {
            logger.warn("no source copied");
            return;
        }
        ofNullable(fileContext.sourceCopiedConsumer())
            .ifPresent(scc -> scc.accept(sourceCopied_[0]));

        final FileContext targetContext = new DefaultFileContext();
        targetContext.fileOperationSupplier(() -> FileOperation.WRITE);
        targetContext.pathNameSupplier(() -> targetPath);
        targetContext.targetPathSupplier(() -> targetPath);
        final Long[] targetCopied_ = new Long[1];
        targetContext.targetChannelConsumer(tc -> {
            try {
                final long targetCopied = Files.copy(
                    tempFile, Channels.newOutputStream(tc));
                logger.debug("target copied: {}", targetCopied);
                targetCopied_[0] = targetCopied;
            } catch (final IOException ioe) {
                logger.error("failed; temp file -> target channel", ioe);
            }
        });
        if (targetCopied_[0] == null) {
            logger.warn("no target copied");
            return;
        }
        ofNullable(fileContext.targetCopiedConsumer())
            .ifPresent(tcc -> tcc.accept(targetCopied_[0]));
    }


    void copyUsing(final FileContext fileContext)
        throws IOException, FileBackException {

        final Path tempFile = Files.createTempFile("prefix", "suffix");
        try {
            copyUsing(fileContext, tempFile);
        } finally {
            final boolean tempDeleted = Files.deleteIfExists(tempFile);
        }
    }


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


    private transient final Logger logger = getLogger(lookup().lookupClass());


    private String keyDigestAlgorithm = DEFAULT_KEY_DIGEST_ALGORITHM;


    private int pathTokeyLength = DEFAULT_PATH_TOKEN_LENGTH;


    private String pathTokenDelimiter = DEFAULT_PATH_TOKEN_DELIMITER;


}

