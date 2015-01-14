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
import static java.lang.invoke.MethodHandles.lookup;
import java.nio.ByteBuffer;
import static java.nio.channels.Channels.newInputStream;
import static java.nio.channels.Channels.newOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import static java.nio.file.Files.newByteChannel;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public class LocalFileBack implements FileBack {


    private static final String KEY_DIGEST_ALGORITHM = "SHA-1"; // 160 bits


    private static final int PATH_TOKEN_LENGTH = 3;


    private static final String PATH_TOKEN_DELIMITER = "/";


    static Path leafPath(final Path rootPath, final ByteBuffer fileKey,
                         final boolean createParent) {

        final Logger logger = getLogger(lookup().lookupClass());

        logger.debug("leafPath({}, {}, {})", rootPath, fileKey, createParent);

        String pathName = null;
        try {
            pathName = FileBackUtilities.fileKeyToPathName(
                fileKey, KEY_DIGEST_ALGORITHM, PATH_TOKEN_LENGTH,
                PATH_TOKEN_DELIMITER);
        } catch (final NoSuchAlgorithmException nsae) {
            throw new RuntimeException(nsae);
        }
        logger.debug("path name: {}", pathName);

        final Path leafPath = rootPath.resolve(pathName.replace(
            PATH_TOKEN_DELIMITER, rootPath.getFileSystem().getSeparator()));
        logger.debug("leaf path: {}", leafPath);

        if (createParent) {
            final Path parent = leafPath.getParent();
            logger.debug("parent: {}", parent);
            if (!Files.isDirectory(parent)) {
                try {
                    final Path created = Files.createDirectories(parent);
                    logger.debug("parent created: {}", created);
                } catch (Exception e) {
                    logger.error("failed to create parent directory: " + parent,
                                 e);
                    throw new RuntimeException(e);
                }
            }
        }

        return leafPath;
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
        logger.debug("file operation: {}", fileOperation);
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


    public void copy(final FileContext fileContext)
        throws IOException, FileBackException {

        logger.debug("copy({})", fileContext);

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path[] sourceLeafPath_ = new Path[1];
        if (sourceLeafPath_[0] == null) {
            ofNullable(fileContext.sourceKeySupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(v -> {
                    logger.debug("source key: {}", v);
                    sourceLeafPath_[0] = leafPath(rootPath, v, false);
                });
            });
        }
        final Path sourceLeafPath = sourceLeafPath_[0];
        logger.debug("source leaf path: {}", sourceLeafPath);
        ofNullable(fileContext.sourceObjectConsumer()).ifPresent(
            c -> c.accept(sourceLeafPath));
        if (sourceLeafPath == null) {
            logger.error("no source leaf path located");
            return;
        }
        if (!Files.isReadable(sourceLeafPath)) {
            logger.error("source leaf path is not readable: {}",
                         sourceLeafPath);
            return;
        }

        final Path[] targetLeafPath_ = new Path[1];
        if (targetLeafPath_[0] == null) {
            ofNullable(fileContext.targetKeySupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(v -> {
                    logger.debug("target key: {}", v);
                    targetLeafPath_[0] = leafPath(rootPath, v, true);
                });
            });
        }
        final Path targetLeafPath = targetLeafPath_[0];
        logger.debug("target leaf path: {}", targetLeafPath);
        ofNullable(fileContext.targetObjectConsumer()).ifPresent(
            c -> c.accept(targetLeafPath));
        if (targetLeafPath == null) {
            logger.error("no target leaf path located");
            return;
        }

        if (sourceLeafPath.equals(targetLeafPath)) {
            logger.error("source leaf path == target leaf path");
            return;
        }

        Files.copy(sourceLeafPath, targetLeafPath,
                   StandardCopyOption.REPLACE_EXISTING);
        logger.debug("file copied");

        final String pathName = StreamSupport
            .stream(((Iterable<Path>) () -> rootPath.relativize(targetLeafPath)
                     .iterator()).spliterator(), false)
            .map(Path::toString).collect(joining("/"));
        logger.debug("path name: {}", pathName);
        ofNullable(fileContext.pathNameConsumer()).ifPresent(
            c -> c.accept(pathName));

        ofNullable(fileContext.sourceCopiedConsumer()).ifPresent(
            c -> c.accept(sourceLeafPath.toFile().length()));
        ofNullable(fileContext.targetCopiedConsumer()).ifPresent(
            c -> c.accept(targetLeafPath.toFile().length()));
    }


    public void delete(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path[] leafPath_ = new Path[1];
        if (leafPath_[0] == null) {
            ofNullable(fileContext.sourceKeySupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(v -> {
                    logger.debug("source key: {}", v);
                    leafPath_[0] = leafPath(rootPath, v, false);
                });
            });
        }
        if (leafPath_[0] == null) {
            ofNullable(fileContext.targetKeySupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(v -> {
                    logger.debug("target key: {}", v);
                    leafPath_[0] = leafPath(rootPath, v, false);
                });
            });
        }
        final Path leafPath = leafPath_[0];
        logger.debug("leaf path: {}", leafPath);
        ofNullable(fileContext.sourceObjectConsumer()).ifPresent(
            c -> c.accept(leafPath));
        ofNullable(fileContext.targetObjectConsumer()).ifPresent(
            c -> c.accept(leafPath));

        final boolean fileDeleted = Files.deleteIfExists(leafPath);
        logger.debug("file deleted: {}", fileDeleted);
    }


    public void read(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path[] sourceLeafPath_ = new Path[1];
        if (sourceLeafPath_[0] == null) {
            ofNullable(fileContext.sourceKeySupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(v -> {
                    logger.debug("source key: {}", v);
                    sourceLeafPath_[0] = leafPath(rootPath, v, false);
                });
            });
        }
        if (sourceLeafPath_[0] == null) {
            ofNullable(fileContext.pathNameSupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(v -> {
                    logger.debug("path name: {}", v);
                    sourceLeafPath_[0] = rootPath.resolve(v);
                });
            });
        }
        final Path sourceLeafPath = sourceLeafPath_[0];
        logger.debug("source leaf path: {}", sourceLeafPath);
        ofNullable(fileContext.sourceObjectConsumer()).ifPresent(
            c -> c.accept(sourceLeafPath));
        if (sourceLeafPath == null) {
            logger.warn("no source leaf path located");
            return;
        }
        if (!Files.isRegularFile(sourceLeafPath)) {
            logger.warn("source leaf path is not a regular file: {}",
                        sourceLeafPath);
            return;
        }

        final String pathName = StreamSupport
            .stream(((Iterable<Path>) () -> rootPath.relativize(sourceLeafPath)
                     .iterator()).spliterator(), false)
            .map(Path::toString).collect(joining("/"));
        logger.debug("path name: {}", pathName);
        ofNullable(fileContext.pathNameConsumer()).ifPresent(
            c -> c.accept(pathName));

        ofNullable(fileContext.sourceChannelConsumer()).ifPresent(c -> {
            logger.debug("source channel consumer presents");
            try {
                try (ReadableByteChannel sourceChannel = newByteChannel(
                    sourceLeafPath, StandardOpenOption.READ)) {
                    c.accept(sourceChannel);
                }
            } catch (IOException ioe) {
                logger.error(
                    "failed to open source leaf path: " + sourceLeafPath, ioe);
            }
        });

        ofNullable(fileContext.targetChannelSupplier()).ifPresent(s -> {
            logger.debug("target channel supplier presents");
            final WritableByteChannel targetChannel = s.get();
            logger.debug("target channel: {}", targetChannel);
            try {
                final long copied = Files.copy(
                    sourceLeafPath, newOutputStream(targetChannel));
                ofNullable(fileContext.sourceCopiedConsumer()).ifPresent(
                    c -> c.accept(copied));
                ofNullable(fileContext.targetCopiedConsumer()).ifPresent(
                    c -> c.accept(copied));
            } catch (final IOException ioe) {
                logger.error(
                    "failed to copy from source leaf path to target channel",
                    ioe);
            }
        });
    }


    public void write(final FileContext fileContext)
        throws IOException, FileBackException {

        if (fileContext == null) {
            throw new NullPointerException("null fileContext");
        }

        final Path[] targetLeafPath_ = new Path[1];
        if (targetLeafPath_[0] == null) {
            ofNullable(fileContext.targetKeySupplier()).ifPresent(s -> {
                ofNullable(s.get()).ifPresent(v -> {
                    logger.debug("target key: {}", v);
                    targetLeafPath_[0] = leafPath(rootPath, v, true);
                });
            });
        }
        final Path targetLeafPath = targetLeafPath_[0];
        logger.debug("target leaf path: {}", targetLeafPath);
        ofNullable(fileContext.targetObjectConsumer()).ifPresent(
            c -> c.accept(targetLeafPath));
        if (targetLeafPath == null) {
            logger.warn("no target leaf path located");
            return;
        }

        final String pathName = StreamSupport
            .stream(((Iterable<Path>) () -> rootPath.relativize(targetLeafPath)
                     .iterator()).spliterator(), false)
            .map(Path::toString).collect(joining("/"));
        logger.debug("path name: {}", pathName);
        ofNullable(fileContext.pathNameConsumer()).ifPresent(
            c -> {
                logger.debug("accepting path name consumer");
                c.accept(pathName);
            });

        ofNullable(fileContext.targetChannelConsumer()).ifPresent(c -> {
            logger.debug("target channel consumer presents");
            try {
                try (FileChannel targetChannel = FileChannel.open(
                    targetLeafPath, StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE)) {
                    c.accept(targetChannel);
                    targetChannel.force(true);
                }
            } catch (IOException ioe) {
                logger.error(
                    "failed to open target leaf path: " + targetLeafPath, ioe);
            }
        });

        ofNullable(fileContext.sourceChannelSupplier()).ifPresent(s -> {
            logger.debug("source channel supplier presents");
            final ReadableByteChannel sourceChannel = s.get();
            logger.debug("target channel: {}", sourceChannel);
            try {
                final long copied = Files.copy(
                    newInputStream(sourceChannel), targetLeafPath,
                    StandardCopyOption.REPLACE_EXISTING);
                ofNullable(fileContext.sourceCopiedConsumer()).ifPresent(
                    c -> c.accept(copied));
                ofNullable(fileContext.targetCopiedConsumer()).ifPresent(
                    c -> c.accept(copied));
            } catch (final IOException ioe) {
                logger.error(
                    "failed to copy from source leaf path to target channel",
                    ioe);
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

