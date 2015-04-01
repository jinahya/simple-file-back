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


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import static java.util.Optional.ofNullable;
import javax.inject.Inject;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class LocalFileBack extends AbstractFileBack {


    Path leafPath(final String filePath, final boolean createParent)
        throws IOException {

        final Path leafPath = rootPath.resolve(filePath.replace(
            getPathTokenDelimiter(), rootPath.getFileSystem().getSeparator()));
        logger.debug("leaf path: {}", leafPath);

        if (createParent) {
            final Path parent = leafPath.getParent();
            if (!Files.isDirectory(parent)) {
                try {
                    final Path created = Files.createDirectories(parent);
                    logger.debug("parent created: {}", created);
                } catch (final FileAlreadyExistsException fale) {
                    // ok
                }
            }
        }

        return leafPath;
    }


    @Override
    protected void copy(final FileContext fileContext)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        final String sourcePath
            = filePath(fileContext, true, false, true, false);
        logger.debug("source path: {}", sourcePath);

        final String targetPath
            = filePath(fileContext, false, true, false, true);
        logger.debug("target path: {}", targetPath);

        if (targetPath.equals(sourcePath)) {
            throw new FileBackException(
                "target path(" + targetPath + ") equals to source path ("
                + sourcePath + ")");
        }

        final Path sourceObject = leafPath(sourcePath, false);
        final Path targetObject = leafPath(targetPath, true);

        ofNullable(fileContext.sourceObjectConsumer())
            .ifPresent(soc -> soc.accept(sourceObject));
        logger.debug("source object consumer accepted");

        ofNullable(fileContext.targetObjectConsumer())
            .ifPresent(toc -> toc.accept(targetObject));
        logger.debug("target object consumer accepted");

        if (!Files.isRegularFile(sourceObject)) {
            logger.warn("source object is not a regular file: {}",
                        sourceObject);
            return;
        }

        Files.copy(sourceObject, targetObject,
                   StandardCopyOption.REPLACE_EXISTING);
        logger.debug("file copied");

        ofNullable(fileContext.sourceCopiedConsumer()).ifPresent(scc -> {
            scc.accept(sourceObject.toFile().length());
        });
        logger.debug("source copied consumer accepted");

        ofNullable(fileContext.targetCopiedConsumer()).ifPresent(tcc -> {
            tcc.accept(targetObject.toFile().length());
        });
        logger.debug("target copied consumer accepted");
    }


    @Override
    protected void delete(final FileContext fileContext)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        final String filePath = filePath(fileContext, true, true, true, true);
        logger.debug("file path: {}", filePath);

        final Path leafPath = leafPath(filePath, false);
        logger.debug("leaf path: {}", leafPath);

        ofNullable(fileContext.sourceObjectConsumer()).ifPresent(
            soc -> soc.accept(leafPath));
        logger.debug("source object consumer accepted");

        ofNullable(fileContext.targetObjectConsumer()).ifPresent(
            toc -> toc.accept(leafPath));
        logger.debug("target object consumer accepted");

        final boolean fileDeleted = Files.deleteIfExists(leafPath);
        logger.trace("file deleted: {}", fileDeleted);
    }


    @Override
    protected void read(final FileContext fileContext)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        final String sourcePath
            = filePath(fileContext, true, false, true, false);
        logger.debug("source path: {}", sourcePath);

        final Path sourceLeaf = leafPath(sourcePath, false);
        logger.debug("source leaf: {}", sourceLeaf);

        ofNullable(fileContext.sourceObjectConsumer()).ifPresent(
            soc -> soc.accept(sourceLeaf));
        logger.debug("source object consumer accepted with {}", sourceLeaf);

        if (!Files.isRegularFile(sourceLeaf)) {
            logger.warn("source leaf is not a regular file: {}", sourceLeaf);
            return;
        }

        ofNullable(fileContext.sourceChannelConsumer()).ifPresent(scc -> {
            try {
                try (ReadableByteChannel sourceChannel = Files.newByteChannel(
                    sourceLeaf, StandardOpenOption.READ)) {
                    scc.accept(sourceChannel);
                    logger.debug("source channel consumer accepted with {}",
                                 sourceChannel);
                }
            } catch (final IOException ioe) {
                logger.error("failed to accept source channel", ioe);
            }
        });

        ofNullable(fileContext.sourceStreamConsumer()).ifPresent(ssc -> {
            try {
                try (InputStream sourceStream
                    = new FileInputStream(sourceLeaf.toFile())) {
                    ssc.accept(sourceStream);
                    logger.debug("source stream consumer accepted with {}",
                                 sourceStream);
                }
            } catch (final IOException ioe) {
                logger.error("failed to accept source stream", ioe);
            }
        });

        ofNullable(fileContext.targetChannelSupplier()).ifPresent(tcs -> {
            try {
                try (WritableByteChannel targetChannel = tcs.get()) {
                    final long targetCopied = Files.copy(
                        sourceLeaf, Channels.newOutputStream(targetChannel));
                    logger.debug("target copied: {}", targetCopied);
                    ofNullable(fileContext.targetCopiedConsumer()).ifPresent(
                        tcc -> tcc.accept(targetCopied));
                    logger.debug("target copied consumer accepted with {}",
                                 targetCopied);
                }
            } catch (final IOException ioe) {
                logger.error("failed to copy to target channel", ioe);
            }
        });

        ofNullable(fileContext.targetStreamSupplier()).ifPresent(tss -> {
            try {
                try (OutputStream targetStream = tss.get()) {
                    final long targetCopied
                        = Files.copy(sourceLeaf, targetStream);
                    logger.debug("target copied: {}", targetCopied);
                    ofNullable(fileContext.targetCopiedConsumer()).ifPresent(
                        tcc -> tcc.accept(targetCopied));
                    logger.debug("target copied consumer accepted with {}",
                                 targetCopied);
                }
            } catch (final IOException ioe) {
                logger.error("failed to copy to target stream", ioe);
            }
        });
    }


    @Override
    protected void write(final FileContext fileContext)
        throws IOException, FileBackException {

        Objects.requireNonNull(fileContext, "null fileContext");

        final String targetPath
            = filePath(fileContext, false, true, false, true);
        logger.debug("target path: {}", targetPath);

        final Path targetLeaf = leafPath(targetPath, true);
        logger.debug("target leaf: {}", targetLeaf);

        ofNullable(fileContext.targetObjectConsumer()).ifPresent(toc -> {
            toc.accept(targetLeaf);
        });

        ofNullable(fileContext.sourceChannelSupplier()).ifPresent(scs -> {
            try {
                try (ReadableByteChannel sourceChannel = scs.get()) {
                    logger.debug("source channel: {}", sourceChannel);
                    final long sourceCopied = Files.copy(
                        Channels.newInputStream(sourceChannel), targetLeaf,
                        StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("source copied: {}", sourceCopied);
                    ofNullable(fileContext.sourceCopiedConsumer()).ifPresent(
                        scc -> scc.accept(sourceCopied));
                    logger.debug("source copied consumer accepted with {}",
                                 sourceCopied);
                }
            } catch (final IOException ioe) {
                logger.debug("failed to copy from source channel", ioe);
            }
        });

        ofNullable(fileContext.sourceStreamSupplier()).ifPresent(sss -> {
            try {
                try (InputStream sourceStream = sss.get()) {
                    logger.debug("source stream: {}", sourceStream);
                    final long sourceCopied = Files.copy(
                        sourceStream, targetLeaf,
                        StandardCopyOption.REPLACE_EXISTING);
                    logger.debug("source copied: {}", sourceCopied);
                    ofNullable(fileContext.sourceCopiedConsumer()).ifPresent(
                        scc -> scc.accept(sourceCopied));
                    logger.debug("source copied consumer accepted with {}",
                                 sourceCopied);
                }
            } catch (final IOException ioe) {
                logger.debug("failed to copy from source channel", ioe);
            }
        });

        ofNullable(fileContext.targetChannelConsumer()).ifPresent(tcc -> {
            try {
                try (FileChannel targetChannel = FileChannel.open(
                    targetLeaf, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE)) {
                    tcc.accept(targetChannel);
                    logger.debug("target channel consumer acceepted with {}",
                                 targetChannel);
                    targetChannel.force(true);
                    logger.debug("target channel forced");
                }
            } catch (final IOException ioe) {
                logger.error("failed to accept target channel", ioe);
            }
        });

        ofNullable(fileContext.targetStreamConsumer()).ifPresent(tsc -> {
            try {
                try (OutputStream targetStram
                    = new FileOutputStream(targetLeaf.toFile())) {
                    tsc.accept(targetStram);
                    logger.debug("target stream consumer acceepted with {}",
                                 targetStram);
                    targetStram.flush();
                    logger.debug("target channel flushed");
                }
            } catch (final IOException ioe) {
                logger.error("failed to accept target stream", ioe);
            }
        });
    }


    Path rootPath() {

        return rootPath;
    }


    private transient final Logger logger = getLogger(lookup().lookupClass());


    @Inject
    @RootPath
    private Path rootPath;


}

