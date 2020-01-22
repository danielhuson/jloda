/*
 * GraphFX.java Copyright (C) 2020. Daniel H. Huson
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

package jloda.fx.graph;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jloda.graph.*;

/**
 * provides observable list of nodes and edges, and label properties
 * Daniel Huson, 1.20020
 */
public class GraphFX<G extends Graph> {
    private G graph;
    private final ObservableList<Node> nodeList = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<Node> readOnlyNodeList = new ReadOnlyListWrapper<>(nodeList);
    private final ObservableList<Edge> edgeList = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<Edge> readOnlyEdgeList = new ReadOnlyListWrapper<>(edgeList);
    private GraphUpdateListener graphUpdateListener;
    
    private NodeArray<StringProperty> node2LabelProperty;
    private EdgeArray<StringProperty> edge2LabelProperty;

    public GraphFX() {
    }

    public GraphFX(G graph) {
        setGraph(graph);
    }

    public G getGraph() {
        return graph;
    }

    public void setGraph(G graph) {
        if(this.graph!=null && graphUpdateListener!=null) {
            this.graph.removeGraphUpdateListener(graphUpdateListener);
        }

        if(graph!=null) {
            graphUpdateListener = new GraphUpdateAdapter() {
                @Override
                public void newNode(Node v) {
                    Platform.runLater(()->nodeList.add(v));
                }

                @Override
                public void deleteNode(Node v) {
                    Platform.runLater(()->nodeList.remove(v));
            }

                @Override
                public void newEdge(Edge e) {
                    Platform.runLater(()->edgeList.add(e));
                }

                @Override
                public void deleteEdge(Edge e) {
                    Platform.runLater(()->edgeList.remove(e));
                }
                
                @Override
                public void nodeLabelChanged(Node v, String newLabel) {
                    StringProperty stringProperty= node2LabelProperty.get(v);
                    if(stringProperty!=null) {
                        Platform.runLater(()->stringProperty.set(newLabel));
                    }
                }

                @Override
                public void edgeLabelChanged(Edge e, String newLabel) {
                    StringProperty stringProperty = edge2LabelProperty.get(e);
                    if (stringProperty != null) {
                        Platform.runLater(() -> stringProperty.set(newLabel));

                    }
                }
            };
            graph.addGraphUpdateListener(graphUpdateListener);
            node2LabelProperty=new NodeArray<>(graph);
            edge2LabelProperty =new EdgeArray<>(graph);
        }
        else
            node2LabelProperty =null;
        this.graph = graph;
    }

    public ObservableList<Node> getNodeList() {
        return readOnlyNodeList;
    }

    public ObservableList<Edge> getEdgeList() {
        return readOnlyEdgeList;
    }
    
    public StringProperty nodeLabelProperty(Node v) {
        StringProperty stringProperty= node2LabelProperty.get(v);
        if(stringProperty==null) {
            stringProperty=new SimpleStringProperty(graph.getLabel(v));
            node2LabelProperty.put(v, stringProperty);
        }
        return stringProperty;
    }
    public StringProperty edgeLabelProperty(Edge e) {
        StringProperty stringProperty= edge2LabelProperty.get(e);
        if(stringProperty==null) {
            stringProperty=new SimpleStringProperty(graph.getLabel(e));
            edge2LabelProperty.put(e, stringProperty);
        }
        return stringProperty;
    }
}
