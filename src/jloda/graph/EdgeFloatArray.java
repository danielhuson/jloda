/*
 * EdgeFloatArray.java Copyright (C) 2020. Daniel H. Huson
 *
 * (Some code written by other authors, as named in code.)
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
 *
 */

package jloda.graph;

/**
 * edge float array
 * Daniel Huson, 11.2017
 */
public class EdgeFloatArray extends EdgeArray<Float> {
    /**
     * Construct an edge array with default value null
     */
    public EdgeFloatArray(Graph g) {
        super(g);
    }

    /**
     * Construct an edge array for the given graph and set the default value
     */
    public EdgeFloatArray(Graph g, Float defaultValue) {
        super(g, defaultValue);
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeFloatArray(EdgeArray<Float> src) {
        super(src);
    }


    public float get(Edge e) {
        final Float value = getValue(e);
        if (value != null)
            return value;
        else
            return getDefaultValue() != null ? getDefaultValue() : 0f;

    }

    public void set(Edge e, float value) {
        put(e, value);
    }
}

// EOF
