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
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.function.Supplier;
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


    private static final String KEY_DIGEST_ALGORITHM = "SHA-1"; // 160 bits


    private static final int PATH_TOKEN_LENGTH = 3;


    private static final String PATH_TOKEN_DELIMITER = "/";


    static Path leafPath(final Path localRoot, final ByteBuffer keyBuffer,
                         final String fileSuffix, final boolean createParent)
        throws IOException, FileBackException {

        final Logger logger = getLogger(lookup().lookupClass());

        String pathName = null;
        try {
            pathName = FileBackUtilities.keyBufferToPathName(
                keyBuffer, KEY_DIGEST_ALGORITHM, PATH_TOKEN_LENGTH,
                PATH_TOKEN_DELIMITER);
        } catch (final NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
        if (fileSuffix != null) {
            pathName += "." + fileSuffix.trim();
        }
        logger.debug("pathName: {}", pathName);

        final Path leafPath = localRoot.resolve(
            pathName.replace("/", localRoot.getFileSystem().getSeparator()));
        logger.debug("leafPath: {}", leafPath);

        if (createParent) {
            final Path parent = leafPath.getParent();
            if (!Files.isDirectory(parent)) {
                Files.createDirectories(parent);
                logger.debug("parent created: {}", parent);
            }
        }

        return leafPath;
    }


    static Path leafPath(final Path localRoot, final FileContext fileContext,
                         final boolean createParent)
        throws IOException, FileBackException {

        final Logger logger = getLogger(lookup().lookupClass());

        final ByteBuffer keyBuffer = Optional.ofNullable(
            fileContext.keyBufferSupplier()).orElse(() -> null).get();
        logger.debug("keyBuffer: {}", keyBuffer);
        if (keyBuffer == null) {
            throw new FileBackException("no keyBuffer supplied");
        }

        String pathName = null;
        try {
            pathName = FileBackUtilities.keyBufferToPathName(
                keyBuffer, KEY_DIGEST_ALGORITHM, PATH_TOKEN_LENGTH,
                PATH_TOKEN_DELIMITER);
        } catch (final NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
        final Supplier<String> fileSuffixSupplier
            = fileContext.fileSuffixSupplier();
        if (fileSuffixSupplier != null) {
            final String fileSuffix = fileSuffixSupplier.get();
            if (fileSuffix != null) {
                pathName += "." + fileSuffix.trim();
            }
        }
        logger.debug("pathName: {}", pathName);
        Optional.ofNullable(fileContext.pathNameConsumer()).orElse(v -> {
        }).accept(pathName);

        final Path leafPath = localRoot.resolve(
            pathName.replace("/", localRoot.getFileSystem().getSeparator()));
        logger.debug("leafPath: {}", leafPath);

        if (createParent) {
            final Path parent = leafPath.getParent();
            if (!Files.isDirectory(parent)) {
                Files.createDirectories(parent);
                logger.debug("parent created: {}", parent);
            }
        }

        return leafPath;
    }


    //@Override
    public void locate(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        String pathName = Optional.ofNullable(fileContext.pathNameSupplier())
            .orElse(() -> null).get();
        logger.debug("pathName: {}", pathName);
        if (pathName == null) {
            throw new FileBackException("no pathName supplied");
        }

        final Path leafPath = rootPath.resolve(pathName);
        logger.debug("leafPath: {}", leafPath);
        logger.debug("leafPath.regularFile: {}", Files.isRegularFile(leafPath));
        logger.debug("leafPath.readable: {}", Files.isReadable(leafPath));

        if (!Files.isRegularFile(leafPath) || !Files.isReadable(leafPath)) {
            Optional.ofNullable(fileContext.targetCopiedConsumer())
                .ifPresent(c -> c.accept(null));
            return;
        }

        final WritableByteChannel targetChannel = Optional
            .ofNullable(fileContext.targetChannelSupplier())
            .orElseThrow(
                () -> new FileBackException("no targetChannelSupplier"))
            .get();
        logger.debug("targetChannel: {}", targetChannel);
        if (targetChannel == null) {
            throw new FileBackException("null targetChannel supplied");
        }
        final long targetCopied = Files.copy(
            leafPath, Channels.newOutputStream(targetChannel));
        logger.debug("targetCopied: {}", targetCopied);
        Optional.ofNullable(fileContext.targetCopiedConsumer())
            .ifPresent(c -> c.accept(targetCopied));
    }


    @Override
    public void copy(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final String fileSuffix = Optional
            .ofNullable(fileContext.fileSuffixSupplier())
            .orElse(() -> null)
            .get();
        logger.debug("fileSuffix: {}", fileSuffix);

        final ByteBuffer sourceKeyByteBuffer = Optional
            .ofNullable(fileContext.sourceKeyBufferSupplier())
            .orElseThrow(() -> new FileBackException(
                    "no value for "
                    + FileBackConstants.PROPERTY_SOURCE_KEY_BUFFER_SUPPLIER))
            .get();
        logger.debug("sourceKeyByteBuffer: {}", sourceKeyByteBuffer);


        final Path sourceLeafPath = leafPath(rootPath, sourceKeyByteBuffer,
                                             fileSuffix, false);
        logger.debug("sourceLeafPath: {}", sourceLeafPath);
        if (!Files.isRegularFile(sourceLeafPath)) {
            logger.warn("sourceLeafPath is not a regular file: {}",
                        sourceLeafPath);
            return;
        }
        if (!Files.isReadable(sourceLeafPath)) {
            logger.warn("sourceLeafPath is not readable: {}", sourceLeafPath);
            return;
        }

        final ByteBuffer targetKeyByteBuffer = Optional
            .ofNullable(fileContext.sourceKeyBufferSupplier())
            .orElseThrow(() -> new FileBackException(
                    "no value for "
                    + FileBackConstants.PROPERTY_TARGET_KEY_BUFFER_SUPPLIER))
            .get();
        logger.debug("targetKeyByteBuffer: {}", targetKeyByteBuffer);

        final Path targetLeafPath = leafPath(rootPath, targetKeyByteBuffer,
                                             fileSuffix, true);
        logger.debug("targetLeafPath: {}", targetLeafPath);
        if (!Files.isRegularFile(targetLeafPath)) {
            logger.warn("targetLeafPath is not a regular file: {}",
                        targetLeafPath);
            return;
        }
        if (!Files.isReadable(targetLeafPath)) {
            logger.warn("targetLeafPath is not readable: {}", targetLeafPath);
            return;
        }

        Files.copy(sourceLeafPath, targetLeafPath,
                   StandardCopyOption.REPLACE_EXISTING);
    }


    @Override
    public void read(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        //Path leafPath = null;
        final Path[] leafPathHolder = new Path[1];

        if (leafPathHolder[0] == null) {
            logger.debug("trying to use keyBuffer...");
            ofNullable(fileContext.keyBufferSupplier()).ifPresent(s -> {
                final ByteBuffer keyBuffer = s.get();
                logger.debug("keyBuffer: {}", keyBuffer);
                final String fileSuffix
                    = ofNullable(fileContext.fileSuffixSupplier())
                    .orElse(() -> null)
                    .get();
                logger.debug("fileSuffix: {}", fileSuffix);
                try {
                    leafPathHolder[0] = leafPath(
                        rootPath, keyBuffer, fileSuffix, false);
                } catch (IOException | FileBackException e) {
                    logger.error("failed to get leafPath", e);
                }
            });
        }

        if (leafPathHolder[0] == null) {
            logger.debug("trying to use pathName...");
            ofNullable(fileContext.pathNameSupplier()).ifPresent(s -> {
                final String pathName = s.get();
                logger.debug("pathName: {}", pathName);
                leafPathHolder[0] = rootPath.resolve(pathName);
            });
        }

        final Path leafPath = leafPathHolder[0];
        logger.debug("leafPath: {}", leafPath);
        if (leafPath == null) {
            logger.warn("no leafPath located");
            return;
        }

        if (!Files.isReadable(leafPath)) {
            logger.warn("leafPath is not readable: {}", leafPath);
            return;
        }

        try (final ReadableByteChannel sourceChannel = FileChannel.open(leafPath, StandardOpenOption.READ)) {
            ofNullable(fileContext.sourceChannelConsumer())
                .ifPresent(c -> c.accept(sourceChannel));
        }

//        final WritableByteChannel targetChannel = Optional
//            .ofNullable(fileContext.targetChannelSupplier())
//            .orElseThrow(
//                () -> new FileBackException("no targetChannelSupplier"))
//            .get();
//        logger.debug("targetChannel: {}", targetChannel);
//        final long targetCopied = Files.copy(
//            leafPath, Channels.newOutputStream(targetChannel));
//        logger.debug("targetCopied: {}", targetCopied);
//        Optional.ofNullable(fileContext.targetCopiedConsumer())
//            .ifPresent(c -> c.accept(targetCopied));
    }


    @Override
    public void update(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path leafPath = leafPath(rootPath, fileContext, true);

        final ReadableByteChannel sourceChannel = Optional
            .ofNullable(fileContext.sourceChannelSupplier())
            .orElseThrow(
                () -> new FileBackException("no sourceChannelSupplier"))
            .get();
        logger.debug("sourceChannel: {}", sourceChannel);
        if (sourceChannel == null) {
            throw new FileBackException("null sourceChannel supplied");
        }

        final long sourceCopied = Files.copy(
            Channels.newInputStream(sourceChannel), leafPath,
            StandardCopyOption.REPLACE_EXISTING);
        logger.debug("sourceCopied: {}", sourceCopied);
        Optional.ofNullable(fileContext.sourceCopiedConsumer())
            .ifPresent(c -> c.accept(sourceCopied));
    }


    @Override
    public void delete(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path leafPath = leafPath(rootPath, fileContext, false);
        logger.debug("leafPath: {}", leafPath);

        final boolean fileDeleted = Files.deleteIfExists(leafPath);
        logger.debug("fileDeleted: {}", fileDeleted);

        Optional.ofNullable(fileContext.fileDeletedConsumer())
            .ifPresent(c -> c.accept(fileDeleted));
    }


    private transient final Logger logger = getLogger(getClass());


    @Inject
    @RootPath
    private Path rootPath;


}

