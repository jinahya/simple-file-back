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


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;


/**
 * A utility class.
 */
public final class FileBackUtilities {


    /**
     *
     * @param keyBytes
     * @param digestAlgorithm
     * @param identifierFunction the function that encodes digested bytes into a
     * string.
     * @param tokenLength the length of each tokens split from the identified
     * string.
     *
     * @return a stream split identifier.
     *
     * @throws NoSuchAlgorithmException if {@code digestAlgorithm} is unknown.
     */
    public static Stream<String> split(
        final byte[] keyBytes, final String digestAlgorithm,
        final Function<byte[], String> identifierFunction,
        final int tokenLength)
        throws NoSuchAlgorithmException {

        if (keyBytes == null) {
            throw new NullPointerException("null keyBytes");
        }

        if (keyBytes.length == 0) {
            throw new IllegalArgumentException(
                "keyBytes.length(" + keyBytes.length + ") == 0");
        }

        if (tokenLength < 0) {
            throw new IllegalArgumentException(
                "tokenLength(" + tokenLength + ") < 0");
        }

        final byte[] digested
            = MessageDigest.getInstance(digestAlgorithm).digest(keyBytes);
        final String identified = identifierFunction.apply(digested);

        return Stream.of(identified.split("(?<=\\G.{" + tokenLength + "})"));
    }


    /**
     *
     * @param keyBytes
     * @param digestAlgorithm
     * @param identifierFuction
     * @param tokenLength
     * @param tokenDelimiter
     *
     * @return A joined string.
     *
     * @throws NoSuchAlgorithmException
     * @see #split(byte[], java.lang.String, java.util.function.Function, int)
     */
    public static String join(
        final byte[] keyBytes, final String digestAlgorithm,
        final Function<byte[], String> identifierFuction,
        final int tokenLength, final CharSequence tokenDelimiter)
        throws NoSuchAlgorithmException {

        return split(keyBytes, digestAlgorithm, identifierFuction, tokenLength)
            .collect(joining(tokenDelimiter));
    }


//    /**
//     *
//     * @param fileContext
//     * @param propertyName
//     *
//     * @return
//     *
//     * @see FileContext#getProperty(java.lang.String)
//     */
//    public static Object propertyValue(final FileContext fileContext,
//                                       final String propertyName) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        if (fileContext == null) {
//            throw new NullPointerException("null fileContext");
//        }
//
//        final Optional<Object> optional = fileContext.getProperty(propertyName);
//        logger.debug("optional: {} for {}", optional, propertyName);
//        assert optional != null;
//
//        return optional.orElseGet(() -> null);
//    }
//
//
//    public static <T> T propertyValue(final FileContext fileContext,
//                                      final String propertyName,
//                                      final Class<T> propertyType) {
//
//        return propertyType.cast(propertyValue(fileContext, propertyName));
//    }
//
//
//    public static Supplier<byte[]> keyBytesSupplier(
//        final FileContext fileContext) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        @SuppressWarnings("unchecked")
//        final Supplier<byte[]> supplier = (Supplier<byte[]>) propertyValue(
//            fileContext, FileBackConstants.PROPERTY_KEY_BYTES_SUPPLIER,
//            Supplier.class);
//        //logger.debug("keyBytesSupplier: {}", supplier);
//
//        return supplier;
//    }
//
//
//    public static byte[] keyBytes(final FileContext fileContext) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        return keyBytesSupplier(fileContext).get();
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<byte[]> keyBytesSupplier(
//        final FileContext fileContext,
//        final Supplier<byte[]> keyBytesSupplier) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        return (Supplier<byte[]>) fileContext.putProperty(
//            FileBackConstants.PROPERTY_KEY_BYTES_SUPPLIER,
//            keyBytesSupplier).orElseGet(() -> null);
//    }
//
//
//    public static byte[] keyBytes(final FileContext fileContext,
//                                  final byte[] keyBytes) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        return keyBytesSupplier(fileContext, () -> keyBytes).get();
//    }
//
//
//    private static InputStream getSourceStream(final FileContext fileContext,
//                                               final boolean checkChannels) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        try {
//            final Supplier<?> supplier = propertyValue(
//                fileContext, FileBackConstants.PROPERTY_SOURCE_STREAM_SUPPLIER,
//                Supplier.class);
//            logger.debug("sourceStreamSupplier: {}", supplier);
//            try {
//                final InputStream value = (InputStream) supplier.get();
//                logger.debug("sourceStreamSupplier.value: {}", value);
//                if (value != null) {
//                    return value;
//                }
//            } catch (final ClassCastException cce) {
//                cce.printStackTrace(System.err);
//            }
//        } catch (final IllegalArgumentException iae) {
//        }
//
//        if (!checkChannels) {
//            throw new IllegalArgumentException("no source stream found");
//        }
//
//        return Channels.newInputStream(getSourceChannel(fileContext, false));
//    }
//
//
//    public static InputStream getSourceStream(final FileContext fileContext) {
//
//        return getSourceStream(fileContext, true);
//    }
//
//
//    private static ReadableByteChannel getSourceChannel(
//        final FileContext fileContext, final boolean checkStreams) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        try {
//            final Supplier<?> supplier = propertyValue(
//                fileContext, FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER,
//                Supplier.class);
//            logger.debug("sourceChannelSupplier: {}", supplier);
//            try {
//                final ReadableByteChannel value
//                    = (ReadableByteChannel) supplier.get();
//                logger.debug("sourceChannelSupplier.value: {}", value);
//                if (value != null) {
//                    return value;
//                }
//            } catch (final ClassCastException cce) {
//            }
//        } catch (final IllegalArgumentException iae) {
//        }
//
//        if (!checkStreams) {
//            throw new IllegalArgumentException("no source channels found");
//        }
//
//        return Channels.newChannel(getSourceStream(fileContext, false));
//    }
//
//
//    public static ReadableByteChannel getSourceChannel(
//        final FileContext fileContext) {
//
//        return getSourceChannel(fileContext, true);
//    }
//
//
//    private static OutputStream getTargetStream(final FileContext fileContext,
//                                                final boolean checkChannels) {
//
//        try {
//            final Supplier<?> supplier = propertyValue(
//                fileContext, FileBackConstants.PROPERTY_TARGET_STREAM_SUPPLIER,
//                Supplier.class);
//            try {
//                final OutputStream value = (OutputStream) supplier.get();
//                if (value != null) {
//                    return value;
//                }
//            } catch (final ClassCastException cce) {
//            }
//        } catch (final IllegalArgumentException iae) {
//        }
//
//        if (!checkChannels) {
//            throw new IllegalArgumentException("no source stream found");
//        }
//
//        return Channels.newOutputStream(getTargetChannel(fileContext, false));
//    }
//
//
//    public static OutputStream getTargetStream(final FileContext fileContext) {
//
//        return getTargetStream(fileContext, true);
//    }
//
//
//    private static WritableByteChannel getTargetChannel(
//        final FileContext fileContext, final boolean checkStreams) {
//
//        try {
//            final Supplier<?> supplier = propertyValue(
//                fileContext, FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER,
//                Supplier.class);
//            try {
//                final WritableByteChannel value
//                    = (WritableByteChannel) supplier.get();
//                if (value != null) {
//                    return value;
//                }
//            } catch (final ClassCastException cce) {
//            }
//        } catch (final IllegalArgumentException iae) {
//        }
//
//        if (!checkStreams) {
//            throw new IllegalArgumentException("no target channels found");
//        }
//
//        return Channels.newChannel(getTargetStream(fileContext, false));
//    }
//
//
//    public static WritableByteChannel getTargetChannel(
//        final FileContext fileContext) {
//
//        return getTargetChannel(fileContext, true);
//    }
//
//
//    public static Optional<Object> putKeyBytesSupplier(
//        final FileContext fileContext, final Supplier<byte[]> supplier) {
//
//        return fileContext.putProperty(
//            FileBackConstants.PROPERTY_KEY_BYTES_SUPPLIER, supplier);
//    }
//
//
//    public static void acceptPathName(final FileContext fileContext,
//                                      final String pathName) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        final Optional<Object> optional = fileContext.getProperty(
//            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER);
//        if (!optional.isPresent()) {
//            logger.warn("no property for "
//                        + FileBackConstants.PROPERTY_PATH_NAME_CONSUMER);
//            return;
//        }
//
//        final Object value = optional.get();
//        if (!Consumer.class.isInstance(value)) {
//            logger.warn("pathNameConsumer.value: {}", value);
//            return;
//        }
//
//        @SuppressWarnings("unchecked")
//        final Consumer<String> consumer = (Consumer<String>) value;
//        consumer.accept(pathName);
//    }
//
//
//    public static void acceptLocalPath(final FileContext fileContext,
//                                       final Path localPath) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        final Optional<Object> optional = fileContext.getProperty(
//            FileBackConstants.PROPERTY_LOCAL_PATH_CONSUMER);
//        if (!optional.isPresent()) {
//            logger.warn("no property for "
//                        + FileBackConstants.PROPERTY_LOCAL_PATH_CONSUMER);
//            return;
//        }
//
//        final Object value = optional.get();
//        if (!Consumer.class.isInstance(value)) {
//            logger.warn("pathNameConsumer.value: {}", value);
//            return;
//        }
//
//        @SuppressWarnings("unchecked")
//        final Consumer<Path> consumer = (Consumer<Path>) value;
//        consumer.accept(localPath);
//    }
//
//
//    public static void acceptBytesCopied(final FileContext fileContext,
//                                         final long bytesCopied) {
//
//        final Logger logger = getLogger(lookup().lookupClass());
//
//        final Optional<Object> optional = fileContext.getProperty(
//            FileBackConstants.PROPERTY_BYTES_COPIED_CONSUMER);
//        if (!optional.isPresent()) {
//            logger.warn("no property for "
//                        + FileBackConstants.PROPERTY_BYTES_COPIED_CONSUMER);
//            return;
//        }
//
//        final Object value = optional.get();
//        if (!LongConsumer.class.isInstance(value)) {
//            logger.warn("pathNameConsumer.value: {}", value);
//            return;
//        }
//
//        final LongConsumer consumer = (LongConsumer) value;
//        consumer.accept(bytesCopied);
//    }


    private FileBackUtilities() {

        super();
    }


}

