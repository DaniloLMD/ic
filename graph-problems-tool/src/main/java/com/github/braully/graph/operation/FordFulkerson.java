package com.github.braully.graph.operation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.List;

import com.github.braully.graph.UndirectedSparseGraphTO;

public class FordFulkerson implements IGraphOperation {

  static final String type = "Tiago";
  static final String description = "Edmonds-karp";
  
  @Override
  public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
    Map<String, Object> response = new HashMap<>();

    if (!graph.isDirected) {
      response.put("ERROR", "Grafo não orientado");
      return response;
    }
    
    if(graph.getInputData() == null){
        response.put("ERROR", "No input provided");
        return response;
    }
    String[] input = graph.getInputData().trim().split(" ");

    if(input.length != 2){
        response.put("ERROR", "Input invalido: digite dois inteiros separados por espaço (source sink)");
        return response;
    }


    int source = Integer.parseInt(input[0]);
    int sink = Integer.parseInt(input[1]);

    response.put("Source -> Sink", source + " -> " + sink);

    response.put("Vertices", graph.getVertices().toString());
    
    String[] edges = graph.getEdgeString().trim().split(",");
    String pesosFormat = "";

    for (int i = 0; i < graph.getEdgeCount(); i++) {
      pesosFormat += edges[i] + ": " + graph.edgeWeights.get("" + i);
      if (i + 1 != graph.getEdgeCount()) pesosFormat += ", ";
    }
    response.put("Pesos", pesosFormat);



    int maxFlow = edmondsKarp(graph, source, sink);
    response.put("Fluxo Máximo", maxFlow);

    return response;
  }

  private int edmondsKarp(UndirectedSparseGraphTO<Integer, Integer> graph, int source, int sink) {
    Map<Integer, List<Integer>> residualGraph = new HashMap<>();
    Map<Integer, Map<Integer, Integer>> capacity = new HashMap<>();
    Map<Integer, Integer> parent = new HashMap<>();

    for (int vertex = 0; vertex < graph.getVertexCount(); vertex++) {
      residualGraph.put(vertex, new ArrayList<>());
      capacity.put(vertex, new HashMap<>());
    }

    int edgeCount = 0;
    for (String edge : graph.getEdgeString().split(",")) {
      String[] nodes = edge.trim().split("-");
      Integer u = Integer.parseInt(nodes[0].trim());
      Integer v = Integer.parseInt(nodes[1].trim());
      Integer weight = graph.edgeWeights.get("" + edgeCount++);

      residualGraph.get(u).add(v);
      capacity.get(u).put(v, weight);
      capacity.get(v).put(v, 0);
    }

    int maxFlow = 0;
    while(true) {
      parent.clear();
      Queue<Integer> queue = new LinkedList<>();
      queue.add(source);
      parent.put(source, null);


      while(!queue.isEmpty()) {
        Integer u = queue.poll();

        for (Integer v : residualGraph.get(u)) {
          if (!parent.containsKey(v) && capacity.get(u).get(v) > 0) {

            parent.put(v, u);
            if (v.equals(sink)) {
              break;
            }
            queue.add(v);
          }
        }

        if (parent.containsKey(sink)) {
          break;
        }
      }
      if (!parent.containsKey(sink)) {
        break;
      }

      int pathFlow = Integer.MAX_VALUE;
      for (Integer v = sink; !v.equals(source); v = parent.get(v)) {
        Integer u = parent.get(v);
        pathFlow = Math.min(pathFlow, capacity.get(u).get(v));
      }


    for (Integer v = sink; !v.equals(source); v = parent.get(v)) {
      Integer u = parent.get(v);
        capacity.get(u).put(v, capacity.get(u).get(v) - pathFlow);
        capacity.get(v).put(u, capacity.get(v).get(u) + pathFlow);
      }
      
      maxFlow += pathFlow;
    }
    return maxFlow;
  }

  public String getTypeProblem() {
    return type;
  }

  public String getName() {
      return description;
  }
} 