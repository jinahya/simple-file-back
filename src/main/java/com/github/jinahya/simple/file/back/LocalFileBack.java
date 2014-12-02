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
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.invoke.MethodHandles.lookup;
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


    static Path locate(final Path rootPath, final FileContext fileContext,
                       final boolean createParent)
        throws IOException {

        final Logger logger = getLogger(lookup().lookupClass());

        final byte[] keyBytes = FileBackUtilities.getKeyBytes(fileContext);
        logger.debug("keyBytes: {}", Arrays.toString(keyBytes));

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
        fileContext.putProperty(FileBackConstants.PROPERTY_PATH_NAME, pathName);

        final Path locatedPath = rootPath.resolve(
            joined.replace("/", rootPath.getFileSystem().getSeparator()));
        logger.debug("locatedPath: {}", locatedPath);
        fileContext.putProperty(FileBackConstants.PROPERTY_LOCATED_PATH,
                                locatedPath);
        if (createParent) {
            final Path parent = locatedPath.getParent();
            if (!Files.isDirectory(parent)) {
                Files.createDirectories(parent);
                logger.debug("parent created: {}", parent);
            }
        }
        fileContext.putProperty(FileBackConstants.PROPERTY_LOCATED_PATH_EXIST,
                                Files.isRegularFile(locatedPath));
        fileContext.putProperty(
            FileBackConstants.PROPERTY_LOCATED_PATH_LENGTH,
            Files.isRegularFile(locatedPath) ? Files.size(locatedPath) : null);

        return locatedPath;
    }


    public LocalFileBack() {

        super();

        logger = getLogger(getClass());
    }


    @Override
    public void create(final FileContext fileContext) throws IOException {

        throw new UnsupportedOperationException("not supported: create");
    }


    @Override
    public void read(final FileContext fileContext) throws IOException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path locatedPath = locate(rootPath, fileContext, false);

        if (!Files.isReadable(locatedPath)) {
            logger.debug("locatedPath is not readable: {}", locatedPath);
            return;
        }

        final OutputStream targetStream
            = FileBackUtilities.getTargetStream(fileContext);
        logger.debug("targetStream: {}", targetStream);
        if (targetStream == null) {
            throw new NullPointerException("null targetStream");
        }

        final long bytesCopied = Files.copy(locatedPath, targetStream);
        logger.debug("bytesCopied: {}", bytesCopied);
        fileContext.putProperty(FileBackConstants.PROPERTY_BYTES_COPIED,
                                bytesCopied);
    }


    @Override
    public void update(final FileContext fileContext) throws IOException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path locatedPath = locate(rootPath, fileContext, true);

        if (false && !Files.isWritable(locatedPath)) {
            logger.warn("locatedPath is not writable: {}", locatedPath);
            return;
        }

        final InputStream sourceStream
            = FileBackUtilities.getSourceStream(fileContext);
        logger.debug("sourceStream: {}", sourceStream);
        if (sourceStream == null) {
            throw new IllegalArgumentException("null sourceStream");
        }

        final long bytesCopied = Files.copy(
            sourceStream, locatedPath, StandardCopyOption.REPLACE_EXISTING);
        logger.debug("bytesCopied: {}", bytesCopied);
        fileContext.putProperty(FileBackConstants.PROPERTY_BYTES_COPIED,
                                bytesCopied);
    }


    @Override
    public void delete(final FileContext fileContext) throws IOException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path locatedPath = locate(rootPath, fileContext, false);

        final boolean deleted = Files.deleteIfExists(locatedPath);
        logger.debug("deleted: {}", deleted);
    }


    private final transient Logger logger;


    @Inject
    @LocalRootPath
    private Path rootPath;


}

