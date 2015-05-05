/**
 * Copyright 2015, Daniel Huson
 * Author Daniel Huson
 *(Some files contain contributions from other authors, who are then mentioned separately)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package jloda.map;

import jloda.util.Basic;
import jloda.util.ProgressPercentage;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * Base for memory-mapped file-based getters or putters
 * Daniel Huson, 4.2015
 */
public abstract class FileGetterPutterBase {

    protected enum Mode {READ_ONLY, READ_WRITE, CREATE_READ_WRITE, CREATE_READ_WRITE_IN_MEMORY}

    protected static final int BITS = 30;

    protected static final int BLOCK_SIZE = (1 << BITS);
    protected static final long BIT_MASK = (BLOCK_SIZE - 1l);
    protected final ByteBuffer[] buffers;
    protected final FileChannel fileChannel;
    protected final long fileLength;
    protected final File file;

    protected final boolean inMemory;

    /**
     * opens the named file as READ_ONLY
     *
     * @param file
     * @throws IOException
     */
    protected FileGetterPutterBase(File file) throws IOException {
        this(file, 0, Mode.READ_ONLY);
    }

    /**
     * constructor
     *
     * @param file
     * @param fileLength length of file to be created when mode==CREATE_READ_WRITE, otherwise ignored
     * @throws java.io.IOException
     */
    protected FileGetterPutterBase(File file, long fileLength, Mode mode) throws IOException {
        this.file = file;
        this.inMemory = (mode == Mode.CREATE_READ_WRITE_IN_MEMORY);

        final FileChannel.MapMode fileChannelMapMode;

        switch (mode) {
            case CREATE_READ_WRITE_IN_MEMORY:
            case CREATE_READ_WRITE: {
                // create the file and ensure that it has the given size
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.setLength(fileLength);
                raf.close();
                // fall through to READ_WRITE case....
            }
            case READ_WRITE: {
                fileChannel = (new RandomAccessFile(file, "rw")).getChannel();
                fileChannelMapMode = FileChannel.MapMode.READ_WRITE;
                break;
            }
            default:
            case READ_ONLY: {
                fileChannel = (new RandomAccessFile(file, "rw")).getChannel();
                fileChannelMapMode = FileChannel.MapMode.READ_ONLY;
                break;
            }
        }
        // determine file size
        {
            if (!file.exists())
                throw new IOException("No such file: " + file);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            this.fileLength = raf.length();
            raf.close();
            if (fileLength > 0 && this.fileLength != fileLength) {
                throw new IOException("File length expected: " + fileLength + ", got: " + this.fileLength);
            }
        }

        if (inMemory)
            System.err.println("Allocating: " + Basic.getMemorySizeString(this.fileLength));

        final List<ByteBuffer> list = new LinkedList<>();
        long blockNumber = 0;

        while (true) {
            long start = blockNumber * BLOCK_SIZE;
            long remaining = Math.min(BLOCK_SIZE, this.fileLength - start);
            if (remaining > 0) {
                if (inMemory) {
                    try {
                        ByteBuffer buffer = ByteBuffer.allocate((int) remaining);
                        list.add(buffer);
                    } catch (Exception ex) {
                        System.err.println("Memory allocation failed");
                        System.exit(1);
                    }
                }
                else
                    list.add(fileChannel.map(fileChannelMapMode, start, remaining));
            } else
                break;
            blockNumber++;
        }
        buffers = list.toArray(new ByteBuffer[list.size()]);
        if (mode == Mode.CREATE_READ_WRITE)
            erase(); // clear the file
        // System.err.println("Buffers: " + buffers.length);
    }

    protected static int getWhichBuffer(long filePos) {
        return (int) (filePos >>> BITS);
    }

    protected static int getIndexInBuffer(long filePos) {
        return (int) (filePos & BIT_MASK);
    }

    /**
     * close the file
     *
     * @throws java.io.IOException
     */
    public void close() {
        try {
            if (inMemory) {
                fileChannel.close();
                final ProgressPercentage progress = new ProgressPercentage("Writing file: " + file, buffers.length);
                try (OutputStream outs = new BufferedOutputStream(new FileOutputStream(file), 1024000)) {
                    {
                        long total = 0;
                        for (ByteBuffer buffer : buffers)
                            total += buffer.limit();

                        System.err.println("Size: " + Basic.getMemorySizeString(total));
                    }

                    for (ByteBuffer buffer : buffers) {
                        // fileChannel.write(buffer); // only use this if buffer is direct because otherwise a direct copy is constructed during write
                        buffer.position(0);
                        while (true) {
                            try {
                                outs.write(buffer.get());
                            } catch (BufferUnderflowException e) {
                                break; // buffer.get() also checks limit, so no need for us to check limit...
                            } catch (IndexOutOfBoundsException e) // buffer.get() also checks limit, so no need for us to check limit...
                            {
                                break;
                            }
                        }
                        progress.incrementProgress();
                    }
                }
                progress.close();
            }
            for (int i = 0; i < buffers.length; i++)
                buffers[i] = null;
            fileChannel.close();
        } catch (IOException e) {
            Basic.caught(e);
        }
    }

    /**
     * erase file by setting all bytes to 0
     */
    public void erase() {
        byte[] bytes = null;
        for (ByteBuffer buffer : buffers) {
            if (bytes == null || bytes.length < buffer.limit())
                bytes = new byte[buffer.limit()];
            buffer.position(0);
            buffer.put(bytes, 0, buffer.limit());
            buffer.position(0);
        }
    }

    /**
     * length of array
     *
     * @return array length
     * @throws java.io.IOException
     */
    abstract public long limit();

    /**
     * resize a file and fill new bytes with zeros
     *
     * @param file
     * @param newLength
     * @throws java.io.IOException
     */
    protected static void resize(File file, long newLength) throws IOException {
        final long oldLength;
        {
            final RandomAccessFile raf = new RandomAccessFile(file, "r");
            oldLength = raf.length();
            raf.close();
        }

        if (newLength < oldLength) {
            final RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(newLength);
            raf.close();
        } else if (newLength > oldLength) {
            final BufferedOutputStream outs = new BufferedOutputStream(new FileOutputStream(file, true));
            long count = newLength - oldLength;
            while (--count >= 0) {
                outs.write(0);
            }
            outs.close();
            long theLength;
            {
                final RandomAccessFile raf = new RandomAccessFile(file, "r");
                theLength = raf.length();
                raf.close();
            }
            if (theLength != newLength)
                throw new IOException("Failed to resize file to length: " + newLength + ", length is: " + theLength);
        }
    }
}
