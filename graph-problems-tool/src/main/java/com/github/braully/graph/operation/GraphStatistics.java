package com.github.braully.graph.operation;

import com.github.braully.graph.GraphWS;
import com.github.braully.graph.UndirectedSparseGraphTO;
import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.algorithms.shortestpath.DistanceStatistics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;

public class GraphStatistics implements IGraphOperation {

    static final String type = "General";
    static final String description = "Statistics";

    private static final Logger log = Logger.getLogger(GraphWS.class);

    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        /* Processar a buscar pelo hullset e hullnumber */
        Map<String, Object> response = new HashMap<>();
        try {
            double diameter = diameter(graph);
            response.put("Diameter", diameter);
            try {
                int girth = girth(graph);
                if (girth > 0) {
                    response.put("Girth", girth);
                } else {
                    response.put("Girth", "infinity");
                }
            } catch (Exception e) {
                log.warn("fail on girth", e);
            }

            response.put("n", graph.getVertexCount());
            response.put("m", graph.getEdgeCount());

            basicstats(graph, response);
        } catch (Exception ex) {
            log.error(null, ex);
        }
        return response;
    }

    public void basicstats(UndirectedSparseGraphTO<Integer, Integer> graph, Map<String, Object> response) {
        int Lambda = 0, lambda = Integer.MAX_VALUE;
        for (Integer i : (Collection<Integer>) graph.getVertices()) {
            lambda = Math.min(lambda, graph.getNeighborCount(i));
            Lambda = Math.max(Lambda, graph.getNeighborCount(i));
        }
        response.put("δ", lambda);
        response.put("Δ", Lambda);
        response.put("ω", numConnectedComponents(graph));
    }

    /* 
    Reference: 
    https://github.com/jaspervdj/Genus/blob/master/src/genus/FindGirth.java
    https://stackoverflow.com/questions/12890106/find-the-girth-of-a-graph
    
     */
    public int girth(UndirectedSparseGraphTO<Integer, Integer> graph) {
        class Node {

            public int vertex, depth;

            public Node(int vertex, int depth) {
                this.vertex = vertex;
                this.depth = depth;
            }
        }

        String path = "";
        /* Used for labelling vertices. */
        int[] labels = new int[graph.getVertexCount()];

        /* Best (smallest) cycle length. */
        int best = graph.getVertexCount() - 1;

        /* Queue for our BFS. */
        Queue<Node> queue = new LinkedList<Node>();

        /* Start a BFS from every vertex except the last two (not needed). */
        int root = 0;
        while (root < graph.getVertexCount() - 2 && best > 3) {

            /* Reset labels. */
            for (int i = 0; i < labels.length; i++) {
                labels[i] = -1;
            }

            /* Add initial node to the queue. */
            labels[root] = 0;
            queue.add(new Node(root, 0));

            /* Take next item from the queue. */
            Node node = queue.poll();
            while (node != null && best > 3 && (node.depth + 1) * 2 - 1 < best) {

                /* Depth of neighbours. */
                int depth = node.depth + 1;

                /* Check all neighbours. */
                Collection<Integer> vertNeighbors = graph.getNeighbors(node.vertex);
                for (Integer neighbour
                        : vertNeighbors) {
                    /* We haven't seen this neighbour before. */
                    if (labels[neighbour] < 0) {
                        queue.add(new Node(neighbour, depth));
                        labels[neighbour] = depth;
                        /* Cycle with odd number of edges. */
                    } else if (labels[neighbour] == depth - 1) {
                        if (depth * 2 - 1 < best) {
                            best = depth * 2 - 1;
                            path = root + "-" + neighbour;
                        }
                        /* Cycle with even number of edges. */
                    } else if (labels[neighbour] == depth) {
                        if (depth * 2 < best) {
                            best = depth * 2;
                            path = root + "-" + neighbour;
                        }
                    }
                }

                /* Take next item from the queue. */
                node = queue.poll();
            }

            /* Clear the queue and prepare to start a BFS from a next vertex. */
            queue.clear();
            root++;
        }
//        System.out.println("Path: " + path);
//        log.info("Path: " + path);
        /* We don't want any division by zero errors. */
        return best > 0 ? best : 1;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {

        return description;
    }

    public double diameter(UndirectedSparseGraphTO<Integer, Integer> graph) {
        DistanceStatistics distanceStatistics = new DistanceStatistics();
        return distanceStatistics.diameter(graph);
    }

    public int numConnectedComponents(UndirectedSparseGraphTO<Integer, Integer> graph) {
        int ret = 0;
        BFSDistanceLabeler<Integer, Integer> bdl = new BFSDistanceLabeler<>();
        if (graph != null && graph.getVertexCount() > 0) {
            Collection<Integer> vertices = graph.getVertices();
            TreeSet<Integer> verts = new TreeSet<>(vertices);
            while (!verts.isEmpty()) {
                Integer first = verts.first();
                bdl.labelDistances(graph, first);
                for (Integer v : vertices) {
                    if (bdl.getDistance(graph, v) >= 0) {
                        verts.remove(v);
                    }
                }
                ret++;
            }
        }
        return ret;
    }

    public int bigDelta(UndirectedSparseGraphTO clone) {
        int Lambda = 0, lambda = Integer.MAX_VALUE;
        for (Integer i : (Collection<Integer>) clone.getVertices()) {
            lambda = Math.min(lambda, clone.getNeighborCount(i));
            Lambda = Math.max(Lambda, clone.getNeighborCount(i));
        }
        return Lambda;
    }
}
