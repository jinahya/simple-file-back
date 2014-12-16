/*
 * Copyright 2014 Jin Kwon &lt;jinahya_at_gmail.com&gt;.
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


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public final class FileContexts {


//    @SuppressWarnings("unchecked")
//    public static Supplier<ByteBuffer> keyBufferSupplier(
//        final FileContext fileContext) {
//
//        return (Supplier<ByteBuffer>) fileContext.property(
//            FileBackConstants.PROPERTY_KEY_BUFFER_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<ByteBuffer> keyBufferSupplier(
//        final FileContext fileContext,
//        final Supplier<ByteBuffer> keyBytesSupplier) {
//
//        return (Supplier<ByteBuffer>) fileContext.property(
//            FileBackConstants.PROPERTY_KEY_BUFFER_SUPPLIER, keyBytesSupplier)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<String> fileSuffixSupplier(
//        final FileContext fileContext) {
//
//        return (Supplier<String>) fileContext.property(
//            FileBackConstants.PROPERTY_FILE_SUFFIX_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<String> fileSuffixSupplier(
//        final FileContext fileContext,
//        final Supplier<String> fileSuffixSupplier) {
//
//        return (Supplier<String>) fileContext.property(
//            FileBackConstants.PROPERTY_FILE_SUFFIX_SUPPLIER, fileSuffixSupplier)
//            .orElse(null);
//    }
//
//
//    /**
//     *
//     * @param fileContext
//     * @param keyBytes
//     *
//     * @return
//     *
//     * @deprecated Use {@link #keyBufferSupplier(java.util.function.Supplier)}
//     */
//    @Deprecated
//    public static Supplier<ByteBuffer> keyBufferSupplier(
//        final FileContext fileContext, final byte[] keyBytes) {
//
//        return keyBufferSupplier(
//            fileContext,
//            keyBytes == null ? null : () -> ByteBuffer.wrap(keyBytes));
//    }
//
//
//    /**
//     *
//     * @param fileContext
//     * @param keyString
//     *
//     * @return
//     *
//     * @deprecated Use {@link #keyBufferSupplier(java.util.function.Supplier) }
//     */
//    @Deprecated
//    public static Supplier<ByteBuffer> keyBufferSupplier(
//        final FileContext fileContext, final String keyString) {
//
//        return keyBufferSupplier(
//            fileContext, keyString == null
//                         ? null : keyString.getBytes(StandardCharsets.UTF_8));
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Consumer<String> contentTypeConsumer(
//        final FileContext fileContext) {
//
//        return (Consumer<String>) fileContext.property(
//            FileBackConstants.PROPERTY_CONTENT_TYPE_CONSUMER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Consumer<String> contentTypeConsumer(
//        final FileContext fileContext,
//        final Consumer<String> contentTypeConsumer) {
//
//        return (Consumer<String>) fileContext.property(
//            FileBackConstants.PROPERTY_CONTENT_TYPE_CONSUMER,
//            contentTypeConsumer)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<String> contentTypeSupplier(
//        final FileContext fileContext) {
//
//        return (Supplier<String>) fileContext.property(
//            FileBackConstants.PROPERTY_CONTENT_TYPE_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<String> contentTypeSupplier(
//        final FileContext fileContext,
//        final Supplier<String> contentTypeSupplier) {
//
//        return (Supplier<String>) fileContext.property(
//            FileBackConstants.PROPERTY_CONTENT_TYPE_SUPPLIER,
//            contentTypeSupplier)
//            .orElse(null);
//    }
//
//
//    public static LongConsumer contentLengthConsumer(
//        final FileContext fileContext) {
//
//        return (LongConsumer) fileContext.property(
//            FileBackConstants.PROPERTY_CONTENT_LENGTH_CONSUMER)
//            .orElse(null);
//    }
//
//
//    public static LongConsumer contentLengthConsumer(
//        final FileContext fileContext,
//        final LongConsumer contentLengthConsumer) {
//
//        return (LongConsumer) fileContext.property(
//            FileBackConstants.PROPERTY_CONTENT_LENGTH_CONSUMER,
//            contentLengthConsumer)
//            .orElse(null);
//    }
//
//
//    public static LongSupplier contentLengthSupplier(
//        final FileContext fileContext) {
//
//        return (LongSupplier) fileContext.property(
//            FileBackConstants.PROPERTY_CONTENT_LENGTH_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    public static LongSupplier contentLengthSupplier(
//        final FileContext fileContext,
//        final LongSupplier contentLengthSupplier) {
//
//        return (LongSupplier) fileContext.property(
//            FileBackConstants.PROPERTY_CONTENT_LENGTH_SUPPLIER,
//            contentLengthSupplier)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Consumer<Path> localPathConsumer(
//        final FileContext fileContext) {
//
//        return (Consumer<Path>) fileContext.property(
//            FileBackConstants.PROPERTY_LOCAL_PATH_CONSUMER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Consumer<Path> localPathConsumer(
//        final FileContext fileContext, final Consumer<Path> localPathConsumer) {
//
//        return (Consumer<Path>) fileContext.property(
//            FileBackConstants.PROPERTY_LOCAL_PATH_CONSUMER, localPathConsumer)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Consumer<String> pathNameConsumer(
//        final FileContext fileContext) {
//
//        return (Consumer<String>) fileContext.property(
//            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Consumer<String> pathNameConsumer(
//        final FileContext fileContext,
//        final Consumer<String> pathNameConsumer) {
//
//        return (Consumer<String>) fileContext.property(
//            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER, pathNameConsumer)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<ReadableByteChannel> sourceChannelSupplier(
//        final FileContext fileContext) {
//
//        return (Supplier<ReadableByteChannel>) fileContext.property(
//            FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<ReadableByteChannel> sourceChannelSupplier(
//        final FileContext fileContext,
//        final Supplier<ReadableByteChannel> sourceChannelSupplier) {
//
//        return (Supplier<ReadableByteChannel>) fileContext.property(
//            FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER,
//            sourceChannelSupplier)
//            .orElse(null);
//    }
//
//
//    /**
//     *
//     * @param fileContext
//     * @param sourceChannel
//     *
//     * @return
//     *
//     * @deprecated Use {@link #sourceChannelSupplier(java.util.function.Supplier)
//     * }
//     */
//    @Deprecated
//    public static Supplier<ReadableByteChannel> sourceChannelSupplier(
//        final FileContext fileContext,
//        final ReadableByteChannel sourceChannel) {
//
//        return sourceChannelSupplier(
//            fileContext,
//            sourceChannel == null ? null : () -> sourceChannel);
//    }
//
//
//    /**
//     *
//     * @param fileContext
//     * @param sourceStream
//     *
//     * @return
//     *
//     * @deprecated Use {@link #sourceChannelSupplier(java.util.function.Supplier)
//     * }
//     */
//    @Deprecated
//    public static Supplier<ReadableByteChannel> sourceChannelSupplier(
//        final FileContext fileContext, final InputStream sourceStream) {
//
//        return sourceChannelSupplier(
//            fileContext,
//            sourceStream == null ? null : Channels.newChannel(sourceStream));
//    }
//
//
//    /**
//     *
//     * @param fileContext
//     * @param servletRequest
//     *
//     * @return
//     *
//     * @deprecated Use {@link #sourceChannelSupplier(java.util.function.Supplier)
//     * }
//     */
//    @Deprecated
//    public static Supplier<ReadableByteChannel> sourceChannelSupplier(
//        final FileContext fileContext, final ServletRequest servletRequest) {
//
//        return sourceChannelSupplier(
//            fileContext,
//            servletRequest == null ? null : () -> {
//                try {
//                    return Channels.newChannel(servletRequest.getInputStream());
//                } catch (final IOException ioe) {
//                    throw new RuntimeException(ioe);
//                }
//            });
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<WritableByteChannel> targetChannelSupplier(
//        final FileContext fileContext) {
//
//        return (Supplier<WritableByteChannel>) fileContext.property(
//            FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    public static Supplier<WritableByteChannel> targetChannelSupplier(
//        final FileContext fileContext,
//        final Supplier<WritableByteChannel> targetChannelSupplier) {
//
//        return (Supplier<WritableByteChannel>) fileContext.property(
//            FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER,
//            targetChannelSupplier)
//            .orElse(null);
//    }
//
//
//    /**
//     *
//     * @param fileContext
//     * @param servletResponse
//     *
//     * @return
//     *
//     * @deprecated Use {@link #targetChannelSupplier(java.util.function.Supplier)
//     * }
//     */
//    @Deprecated
//    public static Supplier<WritableByteChannel> targetChannelSupplier(
//        final FileContext fileContext, final ServletResponse servletResponse) {
//
//        return targetChannelSupplier(
//            fileContext,
//            servletResponse == null ? null : () -> {
//                try {
//                    return Channels.newChannel(
//                        servletResponse.getOutputStream());
//                } catch (final IOException ioe) {
//                    throw new RuntimeException(ioe);
//                }
//            });
//    }
//
//
//    /**
//     * Returns the consumer mapped to
//     * {@link FileBackConstants#PROPERTY_BYTES_COPIED_CONSUMER}.
//     *
//     * @param fileContext
//     *
//     * @return the property value mapped to
//     * {@link FileBackConstants#PROPERTY_BYTES_COPIED_CONSUMER} or {@code null}
//     * if no mappings found
//     */
//    public static LongConsumer bytesCopiedConsumer(
//        final FileContext fileContext) {
//
//        return (LongConsumer) fileContext.property(
//            FileBackConstants.PROPERTY_BYTES_COPIED_CONSUMER)
//            .orElse(null);
//    }
//
//
//    /**
//     * Sets a consumer for
//     * {@link FileBackConstants#PROPERTY_BYTES_COPIED_CONSUMER}.
//     *
//     * @param fileContext
//     * @param bytesCopiedConsumer new value; {@code null} for deletion.
//     *
//     * @return the previous value mapped to
//     * {@link FileBackConstants#PROPERTY_BYTES_COPIED_CONSUMER}.
//     */
//    public static LongConsumer bytesCopiedConsumer(
//        final FileContext fileContext, final LongConsumer bytesCopiedConsumer) {
//
//        return (LongConsumer) fileContext.property(
//            FileBackConstants.PROPERTY_BYTES_COPIED_CONSUMER,
//            bytesCopiedConsumer)
//            .orElse(null);
//    }


    private FileContexts() {

        super();
    }


}

