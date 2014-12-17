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


import java.io.IOException;
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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class LocalFileBack implements FileBack {


    private static final String PROPERTY_PREFIX
        = FileBackConstants.PROPERTY_PREFIX + "/local_file_back";


    public static final String DIGEST_ALGORITHM = "SHA-1"; // 160 bits


//    public static final Function<byte[], String> IDENTIFIER_FUNCTION
//        = FileBackConstants.IDENTIFIER_ENCODER_HEX;
    /**
     * The fixed token length for splitting identifiers.
     */
    public static final int TOKEN_LENGTH = 3;


    static Path localLeaf(final Path localRoot, final FileContext fileContext,
                          final boolean createParent)
        throws IOException, FileBackException {

        final Logger logger = getLogger(lookup().lookupClass());


        final byte[] digestedBytes;
        {
            final ByteBuffer keyBuffer = Optional.ofNullable(
                fileContext.keyBufferSupplier()).orElse(() -> null).get();
            logger.debug("keyBuffer: {}", keyBuffer);
            if (keyBuffer == null) {
                throw new FileBackException("no keyBuffer supplied");
            }
            try {
                final MessageDigest digest
                    = MessageDigest.getInstance(DIGEST_ALGORITHM);
                digest.update(keyBuffer.asReadOnlyBuffer());
                digestedBytes = digest.digest();
            } catch (final NoSuchAlgorithmException nsae) {
                throw new RuntimeException(nsae);
            }
        }

        final String hexadecimalized
            = IntStream.range(0, digestedBytes.length)
            .collect(() -> new StringBuilder(digestedBytes.length * 2),
                     (b, i) -> new Formatter(b).format(
                         "%02x", digestedBytes[i] & 0xFF),
                     StringBuilder::append).toString();
        logger.debug("hexadecimalized: {}", hexadecimalized);

        final String joined = Stream.of(
            hexadecimalized.split("(?<=\\G.{" + TOKEN_LENGTH + "})"))
            .collect(Collectors.joining("/"));
        String pathName = "/" + joined;
        final Supplier<String> fileSuffixSupplier
            = fileContext.fileSuffixSupplier();
        if (fileSuffixSupplier != null) {
            final String fileSuffix = fileSuffixSupplier.get();
            if (fileSuffix != null) {
                pathName += "." + fileSuffixSupplier.get();
            }
        }
        logger.debug("pathName: {}", pathName);
        Optional.ofNullable(fileContext.pathNameConsumer()).orElse(v -> {
        }).accept(pathName);

        final Path localLeaf = localRoot.resolve(
            joined.replace("/", localRoot.getFileSystem().getSeparator()));
        logger.debug("localLeaf: {}", localLeaf);
        Optional.ofNullable(fileContext.localLeafConsumer()).orElse(v -> {
        }).accept(localLeaf);

        if (createParent) {
            final Path parent = localLeaf.getParent();
            if (!Files.isDirectory(parent)) {
                Files.createDirectories(parent);
                logger.debug("parent created: {}", parent);
            }
        }

        return localLeaf;
    }


    public LocalFileBack() {

        super();

        logger = getLogger(getClass());
    }


    @Override
    public void create(final FileContext fileContext)
        throws IOException, FileBackException {

        throw new UnsupportedOperationException("not supported: create");
    }


    @Override
    public void read(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path localLeaf = localLeaf(localRoot, fileContext, false);
        if (!Files.isReadable(localLeaf)) {
            logger.error("localLeaf is not readable: {}", localLeaf);
            return;
        }

        final WritableByteChannel targetChannel = Optional.ofNullable(
            fileContext.targetChannelSupplier()).orElse(() -> null).get();
        logger.debug("targetChannel: {}", targetChannel);
        if (targetChannel == null) {
            throw new FileBackException("no targetChannel supplied");
        }

        final long bytesCopied = Files.copy(
            localLeaf, Channels.newOutputStream(targetChannel));
        logger.debug("bytesCopied: {}", bytesCopied);
        Optional.ofNullable(fileContext.bytesCopiedConsumer()).orElse(v -> {
        }).accept(bytesCopied);
    }


    @Override
    public void write(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path localLeaf = localLeaf(localRoot, fileContext, true);

        final ReadableByteChannel sourceChannel = Optional.ofNullable(
            fileContext.sourceChannelSupplier()).orElse(() -> null).get();
        logger.debug("sourceChannel: {}", sourceChannel);
        if (sourceChannel == null) {
            throw new FileBackException("no sourceChannel supplied");
        }

        final long bytesCopied = Files.copy(
            Channels.newInputStream(sourceChannel), localLeaf,
            StandardCopyOption.REPLACE_EXISTING);
        logger.debug("bytesCopied: {}", bytesCopied);
        Optional.ofNullable(fileContext.bytesCopiedConsumer()).orElse(v -> {
        }).accept(bytesCopied);
    }


    @Override
    public void delete(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path localLeaf = localLeaf(localRoot, fileContext, false);
        logger.debug("localLeaf: {}", localLeaf);

        final boolean deleted = Files.deleteIfExists(localLeaf);
        logger.debug("deleted: {}", deleted);
    }


    private final transient Logger logger;


    @Inject
    @LocalRoot
    private Path localRoot;


}

