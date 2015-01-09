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


import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public interface FileContext {


    /**
     * Returns the property value mapped to specified {@code name}.
     *
     * @param name the name of the property
     *
     * @return an optional of the property value mapped to specified
     * {@code name}.
     *
     * @see #property(java.lang.String, java.lang.Class)
     */
    Optional<Object> property(String name);


    /**
     * Returns an optional property value mapped to specified {@code name}.
     *
     * @param <T> value type parameter
     * @param name property name.
     * @param type property value type.
     *
     * @return an optional property value.
     *
     * @see #property(java.lang.String)
     */
    default <T> Optional<T> property(final String name, final Class<T> type) {

        if (type == null) {
            throw new NullPointerException("null type");
        }

        return Optional.ofNullable(type.cast(property(name).orElse(null)));
    }


    /**
     * Sets a property value mapped to specified {@code name}. The property will
     * be removed if {@code value} is {@code null}.
     *
     * @param name the name of the property
     * @param value the new value of the property; {@code null} for removing.
     *
     * @return an optional of previous value.
     *
     * @see #property(java.lang.String, java.lang.Object, java.lang.Class)
     */
    Optional<Object> property(String name, Object value);


    /**
     * Sets a property value.
     *
     * @param <T> property value type parameter
     * @param name property name
     * @param value property value
     * @param type property value type
     *
     * @return an optional property value previously mapped to specified
     * {@code name}.
     *
     * @see #property(java.lang.String, java.lang.Object)
     */
    default <T> Optional<T> property(final String name, final T value,
                                     final Class<T> type) {

        if (type == null) {
            throw new NullPointerException("null type");
        }

        return Optional.ofNullable(type.cast(property(name, value)
            .orElse(null)));
    }


    @SuppressWarnings("unchecked")
    default Supplier<FileBack> fileBackSupplier() {

        return (Supplier<FileBack>) property(
            FileBackConstants.PROPERTY_FILE_BACK_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<FileBack> fileBackSupplier(
        final Supplier<FileBack> fileBackSupplier) {

        return (Supplier<FileBack>) property(
            FileBackConstants.PROPERTY_FILE_BACK_SUPPLIER, fileBackSupplier)
            .orElse(null);
    }


    /**
     * Returns the current property value mapped to
     * {@link FileBackConstants#PROPERTY_KEY_BUFFER_SUPPLIER}.
     *
     * @return the previous value mapped to
     * {@link FileBackConstants#PROPERTY_KEY_BUFFER_SUPPLIER}.
     */
    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> keyBufferSupplier() {

        return (Supplier<ByteBuffer>) property(
            FileBackConstants.PROPERTY_KEY_BUFFER_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> keyBufferSupplier(
        final Supplier<ByteBuffer> keyBytesSupplier) {

        return (Supplier<ByteBuffer>) property(
            FileBackConstants.PROPERTY_KEY_BUFFER_SUPPLIER, keyBytesSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> sourceKeyBufferSupplier() {

        return (Supplier<ByteBuffer>) property(
            FileBackConstants.PROPERTY_SOURCE_KEY_BUFFER_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> sourceKeyBufferSupplier(
        final Supplier<ByteBuffer> sourceKeyBytesSupplier) {

        return (Supplier<ByteBuffer>) property(
            FileBackConstants.PROPERTY_SOURCE_KEY_BUFFER_SUPPLIER,
            sourceKeyBytesSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> targetKeyBufferSupplier() {

        return (Supplier<ByteBuffer>) property(
            FileBackConstants.PROPERTY_TARGET_KEY_BUFFER_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> targetKeyBufferSupplier(
        final Supplier<ByteBuffer> targetKeyBytesSupplier) {

        return (Supplier<ByteBuffer>) property(
            FileBackConstants.PROPERTY_TARGET_KEY_BUFFER_SUPPLIER,
            targetKeyBytesSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> fileSuffixSupplier() {

        return (Supplier<String>) property(
            FileBackConstants.PROPERTY_FILE_SUFFIX_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> fileSuffixSupplier(
        final Supplier<String> fileSuffixSupplier) {

        return (Supplier<String>) property(
            FileBackConstants.PROPERTY_FILE_SUFFIX_SUPPLIER, fileSuffixSupplier)
            .orElse(null);
    }


    /**
     *
     * @param keyBytes
     *
     * @return
     *
     * @deprecated Use {@link #keyBufferSupplier(java.util.function.Supplier)}
     */
    @Deprecated
    default Supplier<ByteBuffer> keyBufferSupplier(final byte[] keyBytes) {

        return keyBufferSupplier(
            keyBytes == null ? null : () -> ByteBuffer.wrap(keyBytes));
    }


    /**
     *
     * @param keyString
     *
     * @return
     *
     * @deprecated Use {@link #keyBufferSupplier(java.util.function.Supplier) }
     */
    @Deprecated
    default Supplier<ByteBuffer> keyBufferSupplier(final String keyString) {

        return keyBufferSupplier(
            keyString == null
            ? null : keyString.getBytes(StandardCharsets.UTF_8));
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> contentTypeConsumer() {

        return (Consumer<String>) property(
            FileBackConstants.PROPERTY_CONTENT_TYPE_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> contentTypeConsumer(
        final Consumer<String> contentTypeConsumer) {

        return (Consumer<String>) property(
            FileBackConstants.PROPERTY_CONTENT_TYPE_CONSUMER,
            contentTypeConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> contentTypeSupplier() {

        return (Supplier<String>) property(
            FileBackConstants.PROPERTY_CONTENT_TYPE_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> contentTypeSupplier(
        final Supplier<String> contentTypeSupplier) {

        return (Supplier<String>) property(
            FileBackConstants.PROPERTY_CONTENT_TYPE_SUPPLIER,
            contentTypeSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> contentLengthConsumer() {

        return (Consumer<Long>) property(
            FileBackConstants.PROPERTY_CONTENT_LENGTH_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> contentLengthConsumer(
        final Consumer<Long> contentLengthConsumer) {

        return (Consumer<Long>) property(
            FileBackConstants.PROPERTY_CONTENT_LENGTH_CONSUMER,
            contentLengthConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<Long> contentLengthSupplier() {

        return (Supplier<Long>) property(
            FileBackConstants.PROPERTY_CONTENT_LENGTH_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<Long> contentLengthSupplier(
        final Supplier<Long> contentLengthSupplier) {

        return (Supplier<Long>) property(
            FileBackConstants.PROPERTY_CONTENT_LENGTH_SUPPLIER,
            contentLengthSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Boolean> fileDeletedConsumer() {

        return (Consumer<Boolean>) property(
            FileBackConstants.PROPERTY_FILE_DELETED_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Boolean> fileDeletedConsumer(
        final Consumer<Boolean> fileDeletedConsumer) {

        return (Consumer<Boolean>) property(
            FileBackConstants.PROPERTY_FILE_DELETED_CONSUMER,
            fileDeletedConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> pathNameConsumer() {

        return (Consumer<String>) property(
            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> pathNameConsumer(
        final Consumer<String> pathNameConsumer) {

        return (Consumer<String>) property(
            FileBackConstants.PROPERTY_PATH_NAME_CONSUMER, pathNameConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> pathNameSupplier() {

        return (Supplier<String>) property(
            FileBackConstants.PROPERTY_PATH_NAME_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> pathNameSupplier(
        final Supplier<String> pathNameSupplier) {

        return (Supplier<String>) property(
            FileBackConstants.PROPERTY_PATH_NAME_SUPPLIER, pathNameSupplier)
            .orElse(null);
    }


//    @SuppressWarnings("unchecked")
//    default Supplier<ReadableByteChannel> sourceChannelSupplier() {
//
//        return (Supplier<ReadableByteChannel>) property(
//            FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    default Supplier<ReadableByteChannel> sourceChannelSupplier(
//        final Supplier<ReadableByteChannel> sourceChannelSupplier) {
//
//        return (Supplier<ReadableByteChannel>) property(
//            FileBackConstants.PROPERTY_SOURCE_CHANNEL_SUPPLIER,
//            sourceChannelSupplier)
//            .orElse(null);
//    }
    @SuppressWarnings("unchecked")
    default Consumer<ReadableByteChannel> sourceChannelConsumer() {

        return (Consumer<ReadableByteChannel>) property(
            FileBackConstants.PROPERTY_SOURCE_CHANNEL_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<ReadableByteChannel> sourceChannelConsumer(
        final Consumer<ReadableByteChannel> sourceChannelSupplier) {

        return (Consumer<ReadableByteChannel>) property(
            FileBackConstants.PROPERTY_SOURCE_CHANNEL_CONSUMER,
            sourceChannelSupplier)
            .orElse(null);
    }


//    /**
//     *
//     * @param sourceChannel
//     *
//     * @return
//     *
//     * @deprecated Use {@link #sourceChannelSupplier(java.util.function.Supplier)
//     * }
//     */
//    @Deprecated
//    default Supplier<ReadableByteChannel> sourceChannelSupplier(
//        final ReadableByteChannel sourceChannel) {
//
//        return sourceChannelSupplier(
//            sourceChannel == null ? null : () -> sourceChannel);
//    }


//    /**
//     *
//     * @param sourceStream
//     *
//     * @return
//     *
//     * @deprecated Use {@link #sourceChannelSupplier(java.util.function.Supplier)
//     * }
//     */
//    @Deprecated
//    default Supplier<ReadableByteChannel> sourceChannelSupplier(
//        final InputStream sourceStream) {
//
//        return sourceChannelSupplier(
//            sourceStream == null ? null : Channels.newChannel(sourceStream));
//    }


//    @SuppressWarnings("unchecked")
//    default Supplier<WritableByteChannel> targetChannelSupplier() {
//
//        return (Supplier<WritableByteChannel>) property(
//            FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    default Supplier<WritableByteChannel> targetChannelSupplier(
//        final Supplier<WritableByteChannel> targetChannelSupplier) {
//
//        return (Supplier<WritableByteChannel>) property(
//            FileBackConstants.PROPERTY_TARGET_CHANNEL_SUPPLIER,
//            targetChannelSupplier)
//            .orElse(null);
//    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> sourceCopiedConsumer() {

        return (Consumer<Long>) property(
            FileBackConstants.PROPERTY_SOURCE_COPIED_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> sourceCopiedConsumer(
        final Consumer<Long> sourceCopiedConsumer) {

        return (Consumer<Long>) property(
            FileBackConstants.PROPERTY_SOURCE_COPIED_CONSUMER,
            sourceCopiedConsumer)
            .orElse(null);
    }


//    @SuppressWarnings("unchecked")
//    default Consumer<Long> targetCopiedConsumer() {
//
//        return (Consumer<Long>) property(
//            FileBackConstants.PROPERTY_TARGET_COPIED_CONSUMER)
//            .orElse(null);
//    }
//
//
//    @SuppressWarnings("unchecked")
//    default Consumer<Long> targetCopiedConsumer(
//        final Consumer<Long> targetCopiedConsumer) {
//
//        return (Consumer<Long>) property(
//            FileBackConstants.PROPERTY_TARGET_COPIED_CONSUMER,
//            targetCopiedConsumer)
//            .orElse(null);
//    }


}

