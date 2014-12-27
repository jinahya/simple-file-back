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
import java.util.Optional;
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

        String pathName = FileBackUtilities.keyBufferToPathName(keyBuffer);
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
//        Optional.ofNullable(fileContext.localLeafConsumer()).orElse(v -> {
//        }).accept(localLeaf);

        if (createParent) {
            final Path parent = leafPath.getParent();
            if (!Files.isDirectory(parent)) {
                Files.createDirectories(parent);
                logger.debug("parent created: {}", parent);
            }
        }

        return leafPath;
    }


    @Override
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
//        Optional.ofNullable(fileContext.localLeafConsumer()).orElse(v -> {
//        }).accept(localLeaf);

        if (!Files.isRegularFile(leafPath) || !Files.isReadable(leafPath)) {
            Optional.ofNullable(fileContext.targetCopiedConsumer())
                .orElse(v -> {
                }).accept(-1L);
            return;
        }

        final WritableByteChannel targetChannel = Optional.ofNullable(
            fileContext.targetChannelSupplier()).orElse(() -> null).get();
        logger.debug("targetChannel: {}", targetChannel);
        if (targetChannel == null) {
            throw new FileBackException("no targetChannel supplied");
        }
        final long targetCopied = Files.copy(
            leafPath, Channels.newOutputStream(targetChannel));
        logger.debug("targetCopied: {}", targetCopied);
        Optional.ofNullable(fileContext.targetCopiedConsumer()).orElse(v -> {
        }).accept(targetCopied);
    }


    @Override
    public void read(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path leafPath = leafPath(rootPath, fileContext, false);

        if (!Files.isReadable(leafPath)) {
            Optional.ofNullable(fileContext.targetCopiedConsumer())
                .orElse(v -> {
                }).accept(-1L);
            logger.error("leafPath is not readable: {}", leafPath);
            return;
        }

        final WritableByteChannel targetChannel = Optional.ofNullable(
            fileContext.targetChannelSupplier()).orElse(() -> null).get();
        logger.debug("targetChannel: {}", targetChannel);
        if (targetChannel == null) {
            throw new FileBackException("no targetChannel supplied");
        }
        final long targetCopied = Files.copy(
            leafPath, Channels.newOutputStream(targetChannel));
        logger.debug("targetCopied: {}", targetCopied);
        Optional.ofNullable(fileContext.targetCopiedConsumer()).orElse(v -> {
        }).accept(targetCopied);
    }


    @Override
    public void update(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path leafPath = leafPath(rootPath, fileContext, true);

        final ReadableByteChannel sourceChannel = Optional.ofNullable(
            fileContext.sourceChannelSupplier()).orElse(() -> null).get();
        logger.debug("sourceChannel: {}", sourceChannel);
        if (sourceChannel == null) {
            throw new FileBackException("no sourceChannel supplied");
        }

        final long sourceCopied = Files.copy(
            Channels.newInputStream(sourceChannel), leafPath,
            StandardCopyOption.REPLACE_EXISTING);
        logger.debug("sourceCopied: {}", sourceCopied);
        Optional.ofNullable(fileContext.sourceCopiedConsumer())
            .orElse(v -> {
            }).accept(sourceCopied);
    }


    @Override
    public void delete(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path leafPath = leafPath(rootPath, fileContext, false);
        logger.debug("leafPath: {}", leafPath);

        final boolean deleted = Files.deleteIfExists(leafPath);
        logger.debug("deleted: {}", deleted);
    }


    private transient final Logger logger = getLogger(getClass());


    @Inject
    @RootPath
    private Path rootPath;


}

