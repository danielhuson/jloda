/*
 *  Copyright (C) 2015 Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jloda.map;

import jloda.util.CanceledException;
import jloda.util.ProgressPercentage;

import java.io.*;

/**
 * int file getter in memory
 * Daniel Huson, 5.2015
 */
public class IntFileGetterInMemory implements IIntGetter {
    private final int BITS = 30;
    private final long BIT_MASK = (1L << (long) BITS) - 1L;

    private final int[][] data;
    private final long limit;

    /**
     * int file getter in memory
     *
     * @param file
     * @throws IOException
     * @throws CanceledException
     */
    public IntFileGetterInMemory(File file) throws IOException {
        limit = file.length() / 4;

        data = new int[(int) ((limit >>> BITS)) + 1][];
        int length0 = (int) (Math.min(limit, 1 << BITS));
        for (int i = 0; i < data.length; i++) {
            int length = Math.min(length0, (int) (limit & BIT_MASK) + 1);
            data[i] = new int[length];
        }

        try (InputStream ins = new BufferedInputStream(new FileInputStream(file))) {
            final ProgressPercentage progress = new ProgressPercentage("Reading file: " + file, limit);
            int whichArray = 0;
            int indexInArray = 0;
            int[] row = data[0];
            for (long index = 0; index < limit; index++) {
                row[indexInArray] = ((ins.read() & 0xFF) << 24) + (((ins.read()) & 0xFF) << 16) + (((ins.read()) & 0xFF) << 8) + (ins.read() & 0xFF);
                if (++indexInArray == length0) {
                    row = data[++whichArray];
                    indexInArray = 0;
                }
                progress.setProgress(index);
            }
            progress.close();
        }
    }

    /**
     * gets value for given index
     *
     * @param index
     * @return value or 0
     */
    @Override
    public int get(long index) {
        return data[(int) (index >>> BITS)][(int) (index & BIT_MASK)];
    }

    /**
     * length of array
     *
     * @return array length
     * @throws IOException
     */
    @Override
    public long limit() {
        return limit;
    }

    /**
     * close the array
     */
    @Override
    public void close() {
    }
}
