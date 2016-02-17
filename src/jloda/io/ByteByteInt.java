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
package jloda.io;

import java.io.UnsupportedEncodingException;

/**
 * two bytes and an integer, used for RefSeqs and similar accession numbers.
 * E.g.,  XP_12345.1 will be coded as byte1=X, byte2=P and value=12345. The trailing stuff .1 is lost
 * Daniel Huson, 1.2009
 */
public class ByteByteInt {
    private byte byte1;
    private byte byte2;
    private int value;
    public static final ByteByteInt ZERO = new ByteByteInt();

    /**
     * constructor
     */
    public ByteByteInt() {
    }

    /**
     * constructor
     *
     * @param byte1
     * @param byte2
     * @param value
     */
    public ByteByteInt(byte byte1, byte byte2, int value) {
        this.byte1 = byte1;
        this.byte2 = byte2;
        this.value = value;
    }

    /**
     * construct a ByteByteInt object from a string.
     * E.g. XP_12345.1 will be parsed as byte1=X, byte2=P and value=12345
     *
     * @param string
     */
    public ByteByteInt(String string) {
        if (string != null && string.length() >= 2) {
            try {
                byte[] bytes = string.substring(0, 2).getBytes("UTF-8");
                byte1 = bytes[0];
                byte2 = bytes[1];
            } catch (UnsupportedEncodingException ex) {
//                Basic.caught(ex);
            }
            int a = 0;
            int b = 0;
            for (int pos = 2; pos < string.length(); pos++) {
                if (a == 0) {
                    if (Character.isDigit(string.charAt(pos)))
                        a = pos;
                } else {
                    if (!Character.isDigit(string.charAt(pos))) {
                        b = pos;
                        break;
                    }
                }
            }
            if (a > 0) {
                if (b == 0)
                    b = string.length();
                try {
                    value = Integer.parseInt(string.substring(a, b));
                } catch (NumberFormatException ex) {
//                    Basic.caught(ex);
                }
            }
        }
    }

    public final byte getByte1() {
        return byte1;
    }

    public final void setByte1(byte byte1) {
        this.byte1 = byte1;
    }

    public final byte getByte2() {
        return byte2;
    }

    public final void setByte2(byte byte2) {
        this.byte2 = byte2;
    }

    public final int getValue() {
        return value;
    }

    public final void setValue(int value) {
        this.value = value;
    }

    public String toString() {
        if (byte1 == 0)
            return "";
        else
            return "" + (char) byte1 + (char) byte2 + "_" + value;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteByteInt that = (ByteByteInt) o;

        if (byte1 != that.byte1) return false;
        return byte2 == that.byte2 && value == that.value;

    }

    public int hashCode() {
        int result;
        result = (int) byte1;
        result = 31 * result + (int) byte2;
        result = 31 * result + value;
        return result;
    }
}
