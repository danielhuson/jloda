/**
 * NodeEdgeEnumeration.java 
 * Copyright (C) 2015 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package jloda.graph;

/**
 * @version $Id: NodeEdgeEnumeration.java,v 1.4 2005-01-07 14:23:05 huson Exp $
 *
 * @author Daniel Huson
 *
 */


/**
 * NodeEdgeEnumeration implements a Enumeration for nodes and edges
 * Daniel Huson, 2003
 */

class NodeEdgeItem {
    /**Constructor of NodeEdgeItem
     * @param ne0 NodeEdge
     * @param next0 NodeEdgeItem
     */
    NodeEdgeItem(NodeEdge ne0, NodeEdgeItem next0) {
        ne = ne0;
        next = next0;
    }

    final NodeEdge ne;
    final NodeEdgeItem next;
}

// EOF
