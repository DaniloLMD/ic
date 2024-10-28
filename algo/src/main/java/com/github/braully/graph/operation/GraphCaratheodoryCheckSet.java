package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphCaratheodoryCheckSet implements IGraphOperation {

    static final String type = "P3-Convexity";
    static final String description = "Caratheodory Check Set(S)";

    public static final int NEIGHBOOR_COUNT_INCLUDED = 1;
    public static final int INCLUDED = 2;
    public static final int PROCESSED = 3;

    public static boolean verbose = false;

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graphRead) {
        long totalTimeMillis = -1;
        Collection<Integer> set = graphRead.getSet();

        totalTimeMillis = System.currentTimeMillis();
        OperationConvexityGraphResult caratheodoryNumberGraph = null;
        if (set.size() >= 2) {
            caratheodoryNumberGraph = hsp3(graphRead, set);
        }
        totalTimeMillis = System.currentTimeMillis() - totalTimeMillis;

        /* Processar a buscar pelo caratheodoryset e caratheodorynumber */
        Map<String, Object> response = new HashMap<>();
        if (caratheodoryNumberGraph == null) {
            caratheodoryNumberGraph = new OperationConvexityGraphResult();
        }
        if (caratheodoryNumberGraph.caratheodorySet != null
                && !caratheodoryNumberGraph.caratheodorySet.isEmpty()) {
            response.putAll(caratheodoryNumberGraph.toMap());
            response.put(OperationConvexityGraphResult.PARAM_NAME_CARATHEODORY_NUMBER, caratheodoryNumberGraph.caratheodorySet.size());
        }
        return response;
    }

    public OperationConvexityGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graph,
            int[] currentSet) {
        OperationConvexityGraphResult processedHullSet = null;
        processedHullSet = hsp3aux(graph, currentSet);
        if (processedHullSet == null || processedHullSet.partial == null || processedHullSet.partial.isEmpty()) {
            processedHullSet = null;
        }
        return processedHullSet;
    }

    public OperationConvexityGraphResult hsp3aux(UndirectedSparseGraphTO<Integer, Integer> graph, int[] currentSet) {
        int currentSetSize = 0;
        OperationConvexityGraphResult processedHullSet = null;
        Set<Integer> hsp3g = new HashSet<>();
        int[] aux = new int[graph.getVertexCount()];
        int[] auxc = new int[graph.getVertexCount()];
        int[] scount = new int[graph.getVertexCount()];
        int maxscount = 0;

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            auxc[i] = 0;
            scount[i] = 0;
        }

        int cont = 0;
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
            auxc[v] = 1;
            currentSetSize++;
            cont++;
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(v);
            for (Integer nb : neighbors) {
                scount[nb]++;
            }
        }
        boolean skip = false;
        for (Integer v : currentSet) {
            if (scount[v] >= 2) {
                skip = true;
                break;
            }
        }
        while (!mustBeIncluded.isEmpty() && !skip) {
            Integer verti = mustBeIncluded.remove();
            hsp3g.add(verti);
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);

            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        mustBeIncluded.add(vertn);
                    }
                    auxc[vertn] = auxc[vertn] + auxc[verti];
                }
            }
            aux[verti] = PROCESSED;
        }
        boolean checkDerivated = false;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (auxc[i] >= currentSet.length && aux[i] == PROCESSED) {
                checkDerivated = true;
                break;
            }
        }

        if (checkDerivated && hsp3g.size() < 2 * currentSet.length - 1) {
            checkDerivated = false;
        }
        Set<Integer> partial = null;
        if (checkDerivated && !skip) {
            partial = calcDerivatedPartial(graph,
                    hsp3g, currentSet);
        }
        Set<Integer> setCurrent = new HashSet<>();
        for (int i : currentSet) {
            setCurrent.add(i);
        }
        processedHullSet = new OperationConvexityGraphResult();
        processedHullSet.caratheodoryNumber = currentSetSize;
        processedHullSet.auxProcessor = aux;
        processedHullSet.convexHull = hsp3g;
        processedHullSet.caratheodorySet = setCurrent;
        processedHullSet.partial = partial;
        return processedHullSet;
    }

    public OperationConvexityGraphResult hsp3aux(UndirectedSparseGraphTO<Integer, Integer> graph, Integer[] currentSet) {
        int currentSetSize = 0;
        OperationConvexityGraphResult processedHullSet = null;
        Set<Integer> hsp3g = new HashSet<>();
        int[] aux = new int[graph.getVertexCount()];
        int[] auxc = new int[graph.getVertexCount()];
        int[] scount = new int[graph.getVertexCount()];
        int maxscount = 0;

        for (int i = 0; i < aux.length; i++) {
            aux[i] = 0;
            auxc[i] = 0;
            scount[i] = 0;
        }

        int cont = 0;
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        for (Integer v : currentSet) {
            mustBeIncluded.add(v);
            aux[v] = INCLUDED;
            auxc[v] = 1;
            currentSetSize++;
            cont++;
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(v);
            for (Integer nb : neighbors) {
                scount[nb]++;
            }
        }
        boolean skip = false;
        for (Integer v : currentSet) {
            if (scount[v] >= 2) {
                skip = true;
                break;
            }
        }
        while (!mustBeIncluded.isEmpty() && !skip) {
            Integer verti = mustBeIncluded.remove();
            hsp3g.add(verti);
            Collection<Integer> neighbors = graph.getNeighborsUnprotected(verti);

            for (int vertn : neighbors) {
                if (vertn == verti) {
                    continue;
                }
                if (vertn != verti && aux[vertn] < INCLUDED) {
                    aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                    if (aux[vertn] == INCLUDED) {
                        mustBeIncluded.add(vertn);
                    }
                    auxc[vertn] = auxc[vertn] + auxc[verti];
                }
            }
            aux[verti] = PROCESSED;
        }
        boolean checkDerivated = false;
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (auxc[i] >= currentSet.length && aux[i] == PROCESSED) {
                checkDerivated = true;
                break;
            }
        }

        if (checkDerivated && hsp3g.size() < 2 * currentSet.length - 1) {
            checkDerivated = false;
        }
        Set<Integer> partial = null;
        if (checkDerivated && !skip) {
            partial = calcDerivatedPartial(graph,
                    hsp3g, currentSet);
        }
        Set<Integer> setCurrent = new HashSet<>();
        for (int i : currentSet) {
            setCurrent.add(i);
        }
        processedHullSet = new OperationConvexityGraphResult();
        processedHullSet.caratheodoryNumber = currentSetSize;
        processedHullSet.auxProcessor = aux;
        processedHullSet.convexHull = hsp3g;
        processedHullSet.caratheodorySet = setCurrent;
        processedHullSet.partial = partial;
        return processedHullSet;
    }

    /**
     * ∂H(S)=H(S)\⋃p∈SH(S\{p})
     *
     * @param graph
     * @param hsp3g
     * @param currentSet
     * @return
     */
    public int maxp = 0;

    public Set<Integer> calcDerivatedPartial(UndirectedSparseGraphTO<Integer, Integer> graph,
            Set<Integer> hsp3g, int[] currentSet) {
        Set<Integer> partial = new HashSet<>();
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        partial.addAll(hsp3g);

        maxp = 0;

        for (Integer p : currentSet) {
            int[] aux = new int[graph.getVertexCount()];
            for (Integer v : currentSet) {
                if (!v.equals(p)) {
                    mustBeIncluded.add(v);
                    aux[v] = INCLUDED;
                }
            }

            List<Integer> list = new ArrayList<>();
            while (!mustBeIncluded.isEmpty() && !partial.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                list.add(verti);
                partial.remove(verti);
                Collection<Integer> neighbors = graph.getNeighbors(verti);
                for (int vertn : neighbors) {
                    if (vertn != verti) {
                        int previousValue = aux[vertn];
                        aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                        if (previousValue < INCLUDED && aux[vertn] >= INCLUDED) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                }
            }
            if (verbose) {
                System.out.print("H(S-" + p + ")=");
//                Collections.sort(list);
                System.out.println(list);

            }
            if (list.size() > maxp) {
                maxp = list.size();
            }
        }
        return partial;
    }

    public Set<Integer> calcDerivatedPartial(UndirectedSparseGraphTO<Integer, Integer> graph,
            Set<Integer> hsp3g, Integer[] currentSet) {
        Set<Integer> partial = new HashSet<>();
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        partial.addAll(hsp3g);

        for (Integer p : currentSet) {
            int[] aux = new int[graph.getVertexCount()];
            for (Integer v : currentSet) {
                if (!v.equals(p)) {
                    mustBeIncluded.add(v);
                    aux[v] = INCLUDED;
                }
            }
            while (!mustBeIncluded.isEmpty() && !partial.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                partial.remove(verti);
                Collection<Integer> neighbors = graph.getNeighbors(verti);
                for (int vertn : neighbors) {
                    if (vertn != verti) {
                        int previousValue = aux[vertn];
                        aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                        if (previousValue < INCLUDED && aux[vertn] >= INCLUDED) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                }
            }
        }
        return partial;
    }

    public Set<Integer> calcDerivatedPartial(UndirectedSparseGraphTO<Integer, Integer> graph,
            Set<Integer> hsp3g, Set<Integer> currentSet) {
        Set<Integer> partial = new HashSet<>();
        Queue<Integer> mustBeIncluded = new ArrayDeque<>();
        partial.addAll(hsp3g);

        for (Integer p : currentSet) {
            int[] aux = new int[graph.getVertexCount()];
            for (Integer v : currentSet) {
                if (!v.equals(p)) {
                    mustBeIncluded.add(v);
                    aux[v] = INCLUDED;
                }
            }
            while (!mustBeIncluded.isEmpty() && !partial.isEmpty()) {
                Integer verti = mustBeIncluded.remove();
                partial.remove(verti);
                Collection<Integer> neighbors = graph.getNeighbors(verti);
                for (int vertn : neighbors) {
                    if (vertn != verti) {
                        int previousValue = aux[vertn];
                        aux[vertn] = aux[vertn] + NEIGHBOOR_COUNT_INCLUDED;
                        if (previousValue < INCLUDED && aux[vertn] >= INCLUDED) {
                            mustBeIncluded.add(vertn);
                        }
                    }
                }
            }
        }
        return partial;
    }

    public OperationConvexityGraphResult hsp3(UndirectedSparseGraphTO<Integer, Integer> graphRead, Collection<Integer> set) {
        int[] arr = new int[set.size()];
        int i = 0;
        for (Integer v : set) {
            arr[i] = v;
            i++;
        }
        return hsp3(graphRead, arr);
    }

    public boolean isCaratheodorySet(UndirectedSparseGraphTO<Integer, Integer> graphRead,
            Set<Integer> buildMaxCaratheodorySet) {
        OperationConvexityGraphResult processedHullSet = hsp3(graphRead, buildMaxCaratheodorySet);
        return processedHullSet != null && processedHullSet.partial != null && !processedHullSet.partial.isEmpty();
    }

    @Override
    public String getTypeProblem() {
        return type;
    }

    @Override
    public String getName() {
        return description;
    }

}
