package com.github.braully.graph.operation;
import com.github.braully.graph.UndirectedSparseGraphTO;
import com.github.braully.graph.WeightedUndirectedSparseGraphTO;
import java.util.HashMap;
import java.util.Map;

public class DaniloWightedGraphNewOperationTest implements IGraphOperation {

    static final String type = "Danilo";
    static final String description = "Operacao em grafo ponderado";


    @Override
    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph) {
        Map<String, Object> response = new HashMap<>();

        if(graph.edgeWeights.size() != graph.getEdgeCount()){
            response.put("ERROR", "Grafo não ponderado");
            return response;

        }

        response.put("Vertices", graph.getVertices().toString());

        String[] edges = graph.getEdgeString().trim().split(",");
        String pesosFormat = "";

        for(int i = 0; i < graph.getEdgeCount(); i++){
            pesosFormat += edges[i] + ": " + graph.edgeWeights.get("" + i);
            if(i+1 != graph.getEdgeCount()) pesosFormat += ", ";
        }

        response.put("Pesos", pesosFormat);


        return response;
    }

    public String getTypeProblem() {
        return type;
    }

    public String getName() {
        return description;
    }
}
