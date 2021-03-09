/*
 *  Dijkstra.java Copyright (C) 2021. Daniel H. Huson
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

package jloda.graphs.algorithms;

import jloda.graphs.interfaces.*;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Dijkstras algorithm for single source shortest path, non-negative edge lengths
 *
 * @author huson
 * Date: 11-Dec-2004
 */
public class Dijkstra {
    /**
     * compute single source shortest path from source to sink, non-negative edge weights
     *
     * @param graph  with adjacentEdges labeled by Integers
     * @param source
     * @param sink
     * @return shortest path from source to sink
     */
    public static <N extends INode, E extends IEdge> List<N> compute(final IGraph<N, E> graph, N source, N sink, Function<E, Number> weights) {
        INodeArray<N, N> predecessor = graph.newNodeArray();

        INodeDoubleArray<N> dist = graph.newNodeDoubleArray();
        SortedSet<N> priorityQueue = newFullQueue(graph, dist);

        // init:
        for (var v : graph.nodes()) {
            dist.put(v, 1000000.0);
            predecessor.put(v, null);
        }
        dist.put(source, 0.0);

        // main loop:
        while (!priorityQueue.isEmpty()) {
            int size = priorityQueue.size();
            N u = priorityQueue.first();
            priorityQueue.remove(u);
            if (priorityQueue.size() != size - 1)
                throw new RuntimeException("remove u=" + u + " failed: size=" + size);

            for (var e : u.outEdges()) {
                double weight = weights.apply((E) e).doubleValue();
                N v = (N) e.getOpposite(u);
                if (dist.getValue(v) > dist.getValue(u) + weight) {
                    // priorty of v changes, so must re-and to queue:
                    priorityQueue.remove(v);
                    dist.put(v, dist.getValue(u) + weight);
                    priorityQueue.add(v);
                    predecessor.put(v, u);
                }
            }
        }
        System.err.println("done main loop");
        final List<N> result = new LinkedList<>();
        var v = sink;
        while (v != source) {
            if (v == null)
                throw new RuntimeException("No path from sink back to source");
            System.err.println("v: " + v);
            if (v != sink)
                result.add(0, v);
            v = predecessor.getValue(v);
        }
        System.err.println("# Dijkstra: " + result.size());
        return result;
    }

    /**
     * setups the priority queue
     *
     * @param graph
     * @param dist
     * @return full priority queue
     * @
     */
    static public <N extends INode, E extends IEdge> SortedSet<N> newFullQueue(final IGraph<N, E> graph, final INodeDoubleArray<N> dist) {
        SortedSet<N> queue = new TreeSet<>((v1, v2) -> {
            double weight1 = dist.getValue(v1);
            double weight2 = dist.getValue(v2);
            //System.out.println("weight1 " + weight1 + " weight2 " + weight2);
            //System.out.println("graph.getId(v1) " + graph.getId(v1) + " graph.getId(v2) " + graph.getId(v2));
            if (weight1 < weight2)
                return -1;
            else if (weight1 > weight2)
                return 1;
            else return Integer.compare(v1.getId(), v2.getId());
        });
        for (var v : graph.nodes())
            queue.add(v);
        return queue;
    }


}
