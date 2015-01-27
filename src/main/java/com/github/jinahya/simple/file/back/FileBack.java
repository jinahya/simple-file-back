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


/**
 * An interface for file operation.
 */
@FunctionalInterface
public interface FileBack {


    /**
     * The file operations.
     */
    public static enum FileOperation {


        /**
         * A constant for coping files.
         */
        COPY,
        /**
         * A constant for deleting files.
         */
        DELETE,
        /**
         * A constant for reading files.
         */
        READ,
        /**
         * A constant for writing files.
         */
        WRITE


    }


    /**
     * Operates a file operation using various properties stored in specified
     * {@code fileContext}.
     *
     * @param fileContext a file context.
     *
     * @throws IOException if an I/O error occurs.
     * @throws FileBackException if a file back error occurs.
     */
    void operate(FileContext fileContext) throws IOException, FileBackException;


}

