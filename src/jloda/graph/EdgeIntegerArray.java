/*
 * EdgeIntegerArray.java Copyright (C) 2020. Daniel H. Huson
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


import jloda.graphs.interfaces.IEdgeIntegerArray;

/**
 * edge float array
 * Daniel Huson, 2003
 */
public class EdgeIntegerArray extends EdgeArray<Integer> implements IEdgeIntegerArray<Edge> {
    /**
     * Construct an edge array with default value null
     */
    public EdgeIntegerArray(Graph g) {
        super(g);
    }

    /**
     * Construct an edge array for the given graph and set the default value
     */
    public EdgeIntegerArray(Graph g, Integer defaultValue) {
        super(g, defaultValue);
    }

    /**
     * Copy constructor.
     *
     * @param src EdgeArray
     */
    public EdgeIntegerArray(EdgeArray<Integer> src) {
        super(src);
    }


    /**
     * get the entry for edge e
     *
     * @param e
     * @return integer or 0
     */
    public int get(Edge e) {
        final Integer value = getValue(e);
        if (value != null)
            return value;
        else
            return getDefaultValue() != null ? getDefaultValue() : 0;
    }

    public void set(Edge e, int value) {
        put(e, value);
    }

    /**
     * increase the count by one.
     *
     * @param e
     */
    public void increment(Edge e) {
        set(e, get(e) + 1);
    }

    /**
     * increase the count by the given value
     *
     * @param e
     */
    public void increment(Edge e, int value) {
        set(e, get(e) + value);
    }

    /**
     * decrease the count by one.
     *
     * @param e
     */
    public void decrement(Edge e) {
        set(e, get(e) - 1);
    }

    /**
     * decrease the count by the given value
     *
     * @param e
     */
    public void decrement(Edge e, int value) {
        set(e, get(e) - value);
    }
}

// EOF
