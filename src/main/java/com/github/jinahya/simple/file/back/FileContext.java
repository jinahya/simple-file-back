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


import com.github.jinahya.simple.file.back.FileBack.FileOperation;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.function.Consumer;
import java.util.function.Supplier;


/**
 * A context between clients and file backs.
 *
 * @author Jin Kwon &lt;jinahya_at_gmail.com&gt;
 */
public interface FileContext {


    /**
     * Properties can be configured within file contexts.
     */
    public static enum PropertyKey {


        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Supplier<FileOperation>} which supplies the target operation.
         */
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Supplier<FileOperation>} which supplies the target operation.
         */
        FILE_OPERATION_SUPPLIER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Consumer<String>} which consumes the path name.
         *
         * @deprecated
         */
        @Deprecated
        PATH_NAME_CONSUMER,
        /**
         * A constant for the key of a property whose values si an instance of
         * {@code Supplier<String>} which supplies the path name.
         *
         * @deprecated
         */
        @Deprecated
        PATH_NAME_SUPPLIER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Consumer<ReadableByteChannel>} which consumes source file
         * channel.
         */
        SOURCE_CHANNEL_CONSUMER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Supplier<RedableByteChanel>} which supplies the source file
         * channel.
         */
        SOURCE_CHANNEL_SUPPLIER,
        /**
         * A constants for the key of a property whose value is an instance of
         * {@code Consumer<Long>} which consumes the number of bytes copied from
         * the source file part.
         */
        SOURCE_COPIED_CONSUMER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Supplier<ByteBuffer>} which supplies the key bytes for
         * locating the source file part.
         */
        SOURCE_KEY_SUPPLIER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Consumer<Object>} which consumes an implementation specific
         * type of source file reference.
         */
        SOURCE_OBJECT_CONSUMER,
        SOURCE_PATH_CONSUMER,
        SOURCE_PATH_SUPPLIER,
        SOURCE_STREAM_CONSUMER,
        SOURCE_STREAM_SUPPLIER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Consumer<WritableByteChannel>} which consumes the target file
         * channel.
         */
        TARGET_CHANNEL_CONSUMER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Supplier<WritableByteChannel>} which supplies a target file
         * channel.
         */
        TARGET_CHANNEL_SUPPLIER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Consumer<Long>} which consumes the number of bytes copied to
         * target file part.
         */
        TARGET_COPIED_CONSUMER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Supplier<ByteBuffer>} which supplies the key bytes the
         * locating the target file part.
         */
        TARGET_KEY_SUPPLIER,
        TARGET_PATH_CONSUMER,
        TARGET_PATH_SUPPLIER,
        /**
         * A constant for the key of a property whose value is an instance of
         * {@code Consumer<Object>} which consumes an implementation specific
         * type of target file reference.
         */
        TARGET_OBJECT_CONSUMER,
        TARGET_STREAM_CONSUMER,
        TARGET_STREAM_SUPPLIER


    }


    /**
     * Returns an optional property value mapped to specified
     * {@code propertyKey}.
     *
     * @param propertyKey the property key
     *
     * @return an optional value.
     */
    Optional<Object> property(PropertyKey propertyKey);


    /**
     * Returns an optional property value mapped to specified
     * {@code propertyKey}.
     *
     * @param <T> value type parameter
     * @param proprtyKey property key
     * @param valueType value type.
     *
     * @return an optional value of property.
     */
    default <T> Optional<T> property(final PropertyKey proprtyKey,
                                     final Class<T> valueType) {

        if (valueType == null) {
            throw new NullPointerException("null type");
        }

        return ofNullable(valueType.cast(property(proprtyKey).orElse(null)));
    }


    /**
     * Sets a new property value mapped to specified {@code propertyKey}. Note
     * that providing {@code null} for {@code propertyValue} will remove
     * previous mapping.
     *
     * @param propertyKey the property key
     * @param propertyValue the property value; {@code null} for removal of
     * mapping.
     *
     * @return an optional property value previously mapped to specified
     * {@code propertyKey}.
     */
    Optional<Object> property(PropertyKey propertyKey, Object propertyValue);


    /**
     * Sets a new property value mapped to specified {@code propertyKey}.
     *
     * @param <T> value type parameter
     * @param propertyKey property key
     * @param propertyValue the new property value.
     * @param valueType value type.
     *
     * @return an optional property value previously mapped to specified
     * {@code propertyKey}.
     */
    default <T> Optional<T> property(final PropertyKey propertyKey,
                                     final T propertyValue,
                                     final Class<T> valueType) {

        if (valueType == null) {
            throw new NullPointerException("null type");
        }

        return ofNullable(valueType.cast(property(propertyKey, propertyValue)
            .orElse(null)));
    }


    /**
     * Return the current property value mapped to
     * {@link PropertyKey#FILE_OPERATION_SUPPLIER}.
     *
     * @return the current value mapped to
     * {@link PropertyKey#FILE_OPERATION_SUPPLIER} or {@code null} if no
     * mappings found.
     */
    @SuppressWarnings("unchecked")
    default Supplier<FileOperation> fileOperationSupplier() {

        return (Supplier<FileOperation>) property(
            PropertyKey.FILE_OPERATION_SUPPLIER)
            .orElse(null);
    }


    /**
     * Sets a new property value mapped to
     * {@link PropertyKey#FILE_OPERATION_SUPPLIER}.
     *
     * @param fileOperationSupplier the new value.
     *
     * @return the previous value mapped to
     * {@link PropertyKey#FILE_OPERATION_SUPPLIER} or {@code null} if there is
     * no mappings.
     */
    @SuppressWarnings("unchecked")
    default Supplier<FileOperation> fileOperationSupplier(
        final Supplier<FileOperation> fileOperationSupplier) {

        return (Supplier<FileOperation>) property(
            PropertyKey.FILE_OPERATION_SUPPLIER, fileOperationSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    @Deprecated
    default Consumer<String> pathNameConsumer() {

        return (Consumer<String>) property(PropertyKey.PATH_NAME_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    @Deprecated
    default Consumer<String> pathNameConsumer(
        final Consumer<String> pathNameConsumer) {

        return (Consumer<String>) property(PropertyKey.PATH_NAME_CONSUMER,
                                           pathNameConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    @Deprecated
    default Supplier<String> pathNameSupplier() {

        return (Supplier<String>) property(PropertyKey.PATH_NAME_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    @Deprecated
    default Supplier<String> pathNameSupplier(
        final Supplier<String> pathNameSupplier) {

        return (Supplier<String>) property(PropertyKey.PATH_NAME_SUPPLIER,
                                           pathNameSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> targetPathConsumer() {

        return (Consumer<String>) (property(PropertyKey.TARGET_PATH_CONSUMER)
                                   .orElse(null));
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> targetPathConsumer(
        final Consumer<String> targetPathConsumer) {

        return (Consumer<String>) (property(PropertyKey.TARGET_PATH_CONSUMER,
                                            targetPathConsumer)
                                   .orElse(null));
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> targetPathSupplier() {

        return (Supplier<String>) (property(PropertyKey.TARGET_PATH_SUPPLIER)
                                   .orElse(null));
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> targetPathSupplier(
        final Supplier<String> targetPathSupplier) {

        return (Supplier<String>) (property(PropertyKey.TARGET_PATH_SUPPLIER,
                                            targetPathSupplier)
                                   .orElse(null));
    }


    @SuppressWarnings("unchecked")
    default Consumer<ReadableByteChannel> sourceChannelConsumer() {

        return (Consumer<ReadableByteChannel>) property(
            PropertyKey.SOURCE_CHANNEL_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<ReadableByteChannel> sourceChannelConsumer(
        final Consumer<ReadableByteChannel> sourceChannelConsumer) {

        return (Consumer<ReadableByteChannel>) property(
            PropertyKey.SOURCE_CHANNEL_CONSUMER, sourceChannelConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ReadableByteChannel> sourceChannelSupplier() {

        return (Supplier<ReadableByteChannel>) property(
            PropertyKey.SOURCE_CHANNEL_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ReadableByteChannel> sourceChannelSupplier(
        final Supplier<ReadableByteChannel> sourceChannelSuppleir) {

        return (Supplier<ReadableByteChannel>) property(
            PropertyKey.SOURCE_CHANNEL_SUPPLIER, sourceChannelSuppleir)
            .orElse(null);
    }


    /**
     * Returns the current property value mapped to
     * {@link PropertyKey#SOURCE_COPIED_CONSUMER}.
     *
     * @return the current property value mapped to
     * {@link PropertyKey#SOURCE_COPIED_CONSUMER} or {@code null} if no mappings
     * found.
     */
    @SuppressWarnings("unchecked")
    default Consumer<Long> sourceCopiedConsumer() {

        return (Consumer<Long>) property(PropertyKey.SOURCE_COPIED_CONSUMER)
            .orElse(null);
    }


    /**
     * Sets the new value for {@link PropertyKey#SOURCE_COPIED_CONSUMER}.
     *
     * @param sourceCopiedConsumer the new value; {@code null} for removal of
     * entry.
     *
     * @return previous value mapped; possibly {@code null}.
     */
    @SuppressWarnings("unchecked")
    default Consumer<Long> sourceCopiedConsumer(
        final Consumer<Long> sourceCopiedConsumer) {

        return (Consumer<Long>) property(PropertyKey.SOURCE_COPIED_CONSUMER,
                                         sourceCopiedConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> sourceKeySupplier() {

        return (Supplier<ByteBuffer>) property(PropertyKey.SOURCE_KEY_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> sourceKeySupplier(
        final Supplier<ByteBuffer> sourceKeySupplier) {

        return (Supplier<ByteBuffer>) property(PropertyKey.SOURCE_KEY_SUPPLIER,
                                               sourceKeySupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Object> sourceObjectConsumer() {

        return (Consumer<Object>) property(PropertyKey.SOURCE_OBJECT_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Object> sourceObjectConsumer(
        final Consumer<Object> sourceObjectConsumer) {

        return (Consumer<Object>) property(PropertyKey.SOURCE_OBJECT_CONSUMER,
                                           sourceObjectConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> sourcePathConsumer() {

        return (Consumer<String>) (property(PropertyKey.SOURCE_PATH_CONSUMER)
                                   .orElse(null));
    }


    @SuppressWarnings("unchecked")
    default Consumer<String> sourcePathConsumer(
        final Consumer<String> sourcePathConsumer) {

        return (Consumer<String>) (property(PropertyKey.SOURCE_PATH_CONSUMER,
                                            sourcePathConsumer)
                                   .orElse(null));
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> sourcePathSupplier() {

        return (Supplier<String>) (property(PropertyKey.SOURCE_PATH_SUPPLIER)
                                   .orElse(null));
    }


    @SuppressWarnings("unchecked")
    default Supplier<String> sourcePathSupplier(
        final Supplier<String> sourcePathSupplier) {

        return (Supplier<String>) (property(PropertyKey.SOURCE_PATH_SUPPLIER,
                                            sourcePathSupplier)
                                   .orElse(null));
    }


    @SuppressWarnings("unchecked")
    default Consumer<InputStream> sourceStreamConsumer() {

        return (Consumer<InputStream>) property(
            PropertyKey.SOURCE_STREAM_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<InputStream> sourceStreamConsumer(
        final Consumer<InputStream> sourceStreamConsumer) {

        return (Consumer<InputStream>) property(
            PropertyKey.SOURCE_STREAM_CONSUMER, sourceStreamConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<InputStream> sourceStreamSupplier() {

        return (Supplier<InputStream>) property(
            PropertyKey.SOURCE_STREAM_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<InputStream> sourceStreamSupplier(
        final Supplier<InputStream> sourceStreamSupplier) {

        return (Supplier<InputStream>) property(
            PropertyKey.SOURCE_STREAM_SUPPLIER, sourceStreamSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> targetCopiedConsumer() {

        return (Consumer<Long>) property(PropertyKey.TARGET_COPIED_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Long> targetCopiedConsumer(
        final Consumer<Long> targetCopiedConsumer) {

        return (Consumer<Long>) property(PropertyKey.TARGET_COPIED_CONSUMER,
                                         targetCopiedConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<WritableByteChannel> targetChannelConsumer() {

        return (Consumer<WritableByteChannel>) property(
            PropertyKey.TARGET_CHANNEL_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<WritableByteChannel> targetChannelConsumer(
        final Consumer<WritableByteChannel> targetChannelConsumer) {

        return (Consumer<WritableByteChannel>) property(
            PropertyKey.TARGET_CHANNEL_CONSUMER, targetChannelConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<WritableByteChannel> targetChannelSupplier() {

        return (Supplier<WritableByteChannel>) property(
            PropertyKey.TARGET_CHANNEL_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<WritableByteChannel> targetChannelSupplier(
        final Supplier<WritableByteChannel> targetChannelSupplier) {

        return (Supplier<WritableByteChannel>) property(
            PropertyKey.TARGET_CHANNEL_SUPPLIER, targetChannelSupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> targetKeySupplier() {

        return (Supplier<ByteBuffer>) property(PropertyKey.TARGET_KEY_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<ByteBuffer> targetKeySupplier(
        final Supplier<ByteBuffer> targetKeySupplier) {

        return (Supplier<ByteBuffer>) property(PropertyKey.TARGET_KEY_SUPPLIER,
                                               targetKeySupplier)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Object> targetObjectConsumer() {

        return (Consumer<Object>) property(PropertyKey.TARGET_OBJECT_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<Object> targetObjectConsumer(
        final Consumer<Object> targetObjectConsumer) {

        return (Consumer<Object>) property(PropertyKey.TARGET_OBJECT_CONSUMER,
                                           targetObjectConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<OutputStream> targetStreamConsumer() {

        return (Consumer<OutputStream>) property(
            PropertyKey.TARGET_STREAM_CONSUMER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Consumer<OutputStream> targetStreamConsumer(
        final Consumer<OutputStream> targetStreamConsumer) {

        return (Consumer<OutputStream>) property(
            PropertyKey.TARGET_STREAM_CONSUMER, targetStreamConsumer)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<OutputStream> targetStreamSupplier() {

        return (Supplier<OutputStream>) property(
            PropertyKey.TARGET_STREAM_SUPPLIER)
            .orElse(null);
    }


    @SuppressWarnings("unchecked")
    default Supplier<OutputStream> targetStreamSupplier(
        final Supplier<OutputStream> targetStreamSupplier) {

        return (Supplier<OutputStream>) property(
            PropertyKey.TARGET_STREAM_SUPPLIER, targetStreamSupplier)
            .orElse(null);
    }


}

