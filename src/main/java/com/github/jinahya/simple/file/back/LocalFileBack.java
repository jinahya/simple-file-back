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
import java.util.Base64;
import java.util.function.Function;
import javax.inject.Inject;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 * @author Jin Kwon
 */
public class LocalFileBack implements FileBack {


    private static final String PROPERTY_PREFIX
        = FileBackConstants.PROPERTY_PREFIX + "/local_file_back";


    public static final String DIGEST_ALGORITHM = "SHA-1"; // 160bits/20bytes/40hexes


    public static final Function<byte[], String> IDENTIFIER_FUNCTION
        = d -> Base64.getUrlEncoder().withoutPadding().encodeToString(d);


    /**
     * The fixed token length for splitting identifiers.
     */
    public static final int TOKEN_LENGTH = 3;


    public static Path locate(final Path rootPath, final byte[] keyBytes)
        throws IOException {

        final Logger logger = getLogger(lookup().lookupClass());

        if (rootPath == null) {
            throw new NullPointerException("null rootPath");
        }

        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException(
                "rootPath(" + rootPath + ") is not a directory");
        }

        if (keyBytes == null) {
            throw new NullPointerException("null keyBytes");
        }

        if (keyBytes.length == 0) {
            throw new IllegalArgumentException(
                "keyBytes.length(" + keyBytes.length + ") == 0");
        }

        final Path located;
        {
            try {
                located = rootPath.resolve(FileBackUtilities.join(
                    keyBytes, DIGEST_ALGORITHM, IDENTIFIER_FUNCTION,
                    TOKEN_LENGTH, rootPath.getFileSystem().getSeparator()));
            } catch (final NoSuchAlgorithmException nsae) {
                throw new RuntimeException(nsae);
            }
        }
        logger.debug("located: {}", located);

        final Path parent = located.getParent();
        logger.debug("parent: {}", parent);
        if (!Files.isDirectory(parent)) {
            Files.createDirectories(parent);
            logger.debug("parent created");
        }

        return located;
    }


    public LocalFileBack() {

        super();

        logger = getLogger(getClass());
    }


    @Override
    public void read(final FileContext fileContext) throws IOException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final OutputStream fileTarget
            = FileBackUtilities.getTargetStream(fileContext);
        logger.debug("fileTarget: {}", fileTarget);

        final byte[] keyByts = FileBackUtilities.getKeyBytes(fileContext);
        logger.debug("keyBytes: {}", keyByts);

        final Path locatedPath = locate(rootPath, keyByts);
        logger.debug("locatedPath: {}", locatedPath);
        fileContext.putProperty(FileBackConstants.PROPERTY_LOCATED_PATH,
                                locatedPath);

        final long bytesCopied = Files.copy(locatedPath, fileTarget);
        logger.debug("bytesCopied: {}", bytesCopied);
        fileContext.putProperty(FileBackConstants.PROPERTY_BYTES_COPIED,
                                bytesCopied);
    }


    @Override
    public void write(final FileContext fileContext) throws IOException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final InputStream fileSource
            = FileBackUtilities.getSourceStream(fileContext);
        logger.debug("fileSource: {}", fileSource);

        final byte[] keyBytes = FileBackUtilities.getKeyBytes(fileContext);
        logger.debug("keyBytes: {}", Arrays.toString(keyBytes));

        final Path locatedPath = locate(rootPath, keyBytes);
        logger.debug("locatedPath: {}", locatedPath);
        fileContext.putProperty(FileBackConstants.PROPERTY_LOCATED_PATH,
                                locatedPath);

        final long bytesCopied = Files.copy(
            fileSource, locatedPath, StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
        logger.debug("bytesCopied: {}", bytesCopied);
        fileContext.putProperty(FileBackConstants.PROPERTY_BYTES_COPIED,
                                bytesCopied);
    }


    private final transient Logger logger;


    @Inject
    @LocalRootPath
    private Path rootPath;


}

