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
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.function.Function;
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


    public static final Function<byte[], String> IDENTIFIER_FUNCTION
        = FileBackConstants.IDENTIFIER_HEX;


    /**
     * The fixed token length for splitting identifiers.
     */
    public static final int TOKEN_LENGTH = 3;


    static Path localPath(final Path rootPath, final FileContext fileContext,
                          final boolean createParent)
        throws IOException, FileBackException {

        final Logger logger = getLogger(lookup().lookupClass());

        final byte[] keyBytes = fileContext.keyBytes();
        logger.debug("keyBytes: {}", Arrays.toString(keyBytes));
        if (keyBytes == null) {
            throw new FileBackException("no keyBytes supplied");
        }

        final String joined;
        try {
            joined = FileBackUtilities.join(
                keyBytes, DIGEST_ALGORITHM, IDENTIFIER_FUNCTION,
                TOKEN_LENGTH, "/");
        } catch (final NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
        final String pathName = "/" + joined;
        logger.debug("pathName: {}", pathName);
        fileContext.acceptPathName(() -> pathName);

        final Path localPath = rootPath.resolve(
            joined.replace("/", rootPath.getFileSystem().getSeparator()));
        logger.debug("localPath: {}", localPath);
        fileContext.acceptLocalPath(() -> localPath);

        if (createParent) {
            final Path parent = localPath.getParent();
            if (!Files.isDirectory(parent)) {
                Files.createDirectories(parent);
                logger.debug("parent created: {}", parent);
            }
        }

        return localPath;
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

        final Path localPath = localPath(rootPath, fileContext, false);

        if (!Files.isReadable(localPath)) {
            logger.warn("localPath is not readable: {}", localPath);
            return;
        }

        final WritableByteChannel targetChannel
            = fileContext.targetChannel();
        logger.debug("targetStream: {}", targetChannel);
        if (targetChannel == null) {
            throw new FileBackException("no targetStream");
        }

        final long bytesCopied = Files.copy(
            localPath, Channels.newOutputStream(targetChannel));
        logger.debug("bytesCopied: {}", bytesCopied);
        fileContext.acceptBytesCopied(() -> bytesCopied);
    }


    @Override
    public void update(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path localPath = localPath(rootPath, fileContext, true);

        final ReadableByteChannel sourceChannel = fileContext.sourceChannel();
        logger.debug("sourceChannel: {}", sourceChannel);
        if (sourceChannel == null) {
            throw new FileBackException("no sourceChannel supplied");
        }

        final long bytesCopied = Files.copy(
            Channels.newInputStream(sourceChannel), localPath,
            StandardCopyOption.REPLACE_EXISTING);
        logger.debug("bytesCopied: {}", bytesCopied);
        fileContext.acceptBytesCopied(() -> bytesCopied);
    }


    @Override
    public void delete(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path localPath = localPath(rootPath, fileContext, false);

        final boolean deleted = Files.deleteIfExists(localPath);
        logger.debug("deleted: {}", deleted);
    }


    private final transient Logger logger;


    @Inject
    @LocalRootPath
    private Path rootPath;


}

