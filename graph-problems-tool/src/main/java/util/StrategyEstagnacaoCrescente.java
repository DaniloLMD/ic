package util;

import edu.uci.ics.jung.graph.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author strike
 */
public class StrategyEstagnacaoCrescente extends StrategyEstagnacao implements IGenStrategy {

    @Override
    public String getName() {
        return "Estagnação de Vertice Crescente";
    }

    @Override
    public void estagnarVertice(Processamento processamento) throws IllegalStateException {
        verboseInicioEtapa(processamento);
        System.out.println("Trabalho por fazer: " + processamento.trabalhoPorFazer);
        System.out.println("Estagnando vertice: " + processamento.trabalhoAtual);
        System.out.println("Possibilidades: " + processamento.getOpcoesPossiveisAtuais());
        Collection<Integer> caminhoPercorridoPosicaoAtual = processamento.getCaminhoPercorridoPosicaoAtual();
        if (caminhoPercorridoPosicaoAtual != null && !caminhoPercorridoPosicaoAtual.isEmpty()) {
            System.out.println("Possibilidades já percorridas: " + caminhoPercorridoPosicaoAtual);
        }
        while (trabalhoNaoAcabou(processamento) && processamento.deuPassoFrente()) {
            processamento.getCaminhoPercorridoPosicaoAtual();
            processamento.melhorOpcaoLocal = avaliarMelhorOpcao(processamento);
            try {
                adicionarMellhorOpcao(processamento);
            } catch (UnsupportedOperationException e) {
                System.out.print(e.getMessage());
                return;
            }
        }
        if (trabalhoAcabou(processamento, processamento.trabalhoAtual) && temFuturo(processamento.trabalhoAtual)) {
            processamento.trabalhoPorFazer.remove(processamento.trabalhoAtual);
        }
    }

    @Override
    public void ordenacaoFimEtapa(Processamento processamento) {
        if (processamento.ordenarTrabalhoPorFazerPorPrimeiraOpcao) {
            Collections.sort(processamento.trabalhoPorFazer, getComparatorTrabalhoPorFazer(processamento));
        } else {
            Collections.sort(processamento.trabalhoPorFazer);
        }
    }

    public Comparator<Integer> getComparatorTrabalhoPorFazer(Processamento processamento) {
        if (comparatorTrabalhoPorFazer == null) {
            comparatorTrabalhoPorFazer = new ComparatorTrabalhoPorFazer(processamento.caminhosPossiveis, false);
        }
        return comparatorTrabalhoPorFazer;
    }

    boolean opcaoValida(Processamento processamento) {
        Integer melhorOpcao = processamento.melhorOpcaoLocal;

        if (!processamento.bfsalg.getDistance(processamento.insumo, processamento.trabalhoAtual).equals(0)) {
            throw new IllegalStateException("Estado do bfs incorreto para" + processamento.trabalhoAtual + " " + processamento.getPosicaoAtualAbsoluta());
        }

        if (melhorOpcao == null) {
            processamento.rbcount[0]++;
            if (processamento.verbose) {
                System.out.println("melhor opçao é nula");
            }
            return false;
        }

        int posicao = processamento.getPosicaoAtualAbsoluta();
        int distanciaMelhorOpcao = processamento.bfsalg.getDistance(processamento.insumo, melhorOpcao);
        if (distanciaMelhorOpcao < 4) {
            //Opção do caminho original já não é mais valida
            processamento.rbcount[1]++;
            if (processamento.verbose) {
                System.out.printf("g[%d](%d,%d) ", posicao, processamento.trabalhoAtual, melhorOpcao);
            }
            return false;
        }

        if (processamento.anteciparVazio && processamento.bfsalg.getDistance(processamento.insumo, processamento.trabalhoAtual) == 0) {
            boolean condicao1 = true;
            int dv = processamento.getDvTrabalhoAtual();
            condicao1 = dv <= processamento.bfsalg.depthcount[4];
            if (!condicao1 && processamento.verbose) {
                System.out.printf("*[%d](%d,%d -> rdv=%d 4c=%d) ", posicao,
                        processamento.trabalhoAtual, melhorOpcao, dv,
                        processamento.bfsalg.depthcount[4]);
            }
            if (!condicao1) {
                processamento.rbcount[2]++;
                return false;
            }
        }
        if (processamento.descartarOpcoesNaoOptimais && !processamento.caminhoPercorrido.get(posicao).isEmpty()) {
            Integer escolhaAnterior = ((List<Integer>) processamento.caminhoPercorrido.get(posicao)).get(0);
            List<Integer> rankingAnterior = processamento.historicoRanking.get(posicao).get(escolhaAnterior);
            if (rankingAnterior != null) {
                Integer rankingEscolhaAnterior = rankingAnterior.get(0);
                Integer rankingOpcaoAtual = processamento.getRankingHistorico(posicao, melhorOpcao);
                if (rankingEscolhaAnterior != null && rankingOpcaoAtual != null && rankingOpcaoAtual < rankingEscolhaAnterior) {
                    processamento.rbcount[3]++;
                    if (processamento.verbose) {
                        System.out.printf("o[%d](%d,%d) ", posicao, processamento.trabalhoAtual, melhorOpcao);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    void adicionarMellhorOpcao(Processamento processamento) {
        if (opcaoValida(processamento)) {
            Integer posicaoAtual = processamento.getPosicaoAtualAbsoluta();
            Collection<Integer> subcaminho = processamento.caminhoPercorrido.getOrDefault(posicaoAtual, new ArrayList<>());
            subcaminho.add(processamento.melhorOpcaoLocal);
            if (processamento.verticeComplete(processamento.melhorOpcaoLocal)) {
                throw new IllegalStateException("vertice statured " + posicaoAtual + " " + processamento.trabalhoAtual + " " + processamento.melhorOpcaoLocal);
            }
            Integer aresta = processamento.addEge();
            if (!aresta.equals(posicaoAtual)) {
                throw new IllegalStateException("Edge not added: " + posicaoAtual + " " + processamento.trabalhoAtual + " " + processamento.melhorOpcaoLocal);
            }
            processamento.caminhoPercorrido.putIfAbsent(aresta, subcaminho);
            if (trabalhoAcabou(processamento, processamento.melhorOpcaoLocal)) {
                processamento.trabalhoPorFazer.remove(processamento.melhorOpcaoLocal);
            }
            observadorDeEtapa(aresta, processamento.melhorOpcaoLocal, processamento);
        } else {
            Pair<Integer> ultimoTrabalho = desfazerUltimoTrabalho(processamento);
            if (ultimoTrabalho == null || !ultimoTrabalho.getFirst().equals(processamento.trabalhoAtual)) {
                throw new UnsupportedOperationException("Não é possivel desfazer um trabalho já comitado por outra etapa");
            }
        }
    }

    @Override
    Integer avaliarMelhorOpcao(Processamento processsamento) {
        processsamento.bfsalg.labelDistances(processsamento.insumo, processsamento.trabalhoAtual);
        Collection<Integer> jaSelecionados = processsamento.getCaminhoPercorridoPosicaoAtual();
        sortAndRanking(processsamento);
        Integer melhorOpcao = null;
        List<Integer> opcoesPossiveisAtuais = processsamento.getOpcoesPossiveisAtuais();
        Integer lastAdd = processsamento.getLastAdd();
        for (int i = 0; i < opcoesPossiveisAtuais.size(); i++) {
            Integer val = opcoesPossiveisAtuais.get(i);
            if (lastAdd == null || val.compareTo(lastAdd) == 1) {
                if (!jaSelecionados.contains(val)) {
                    melhorOpcao = val;
                    break;
                }
            }
        }
        return melhorOpcao;
    }

    @Override
    void sortAndRanking(Processamento processamento) {
        if (processamento.rankearOpcoes) {
            rankearOpcoes(processamento);
        }
    }

    @Override
    public void rankearOpcoes(Processamento processamento) throws RuntimeException {
        Integer[] bfs = processamento.bfsalg.bfs;
        int posicaoAtual = processamento.getPosicaoAtualAbsoluta();
        Integer lastAdd = processamento.getLastAdd();
        if (lastAdd == null) {
            lastAdd = 0;
        }

        Collection<Integer> opcoesPassadas = processamento.getCaminhoPercorridoPosicaoAtual();
        Map<Integer, List<Integer>> rankingAtual = processamento.historicoRanking.getOrDefault(posicaoAtual, new HashMap<>());
        processamento.historicoRanking.putIfAbsent(posicaoAtual, rankingAtual);

        if (opcoesPassadas.isEmpty() || rankingAtual.isEmpty()) {
            processamento.getOpcoesPossiveisAtuais().sort(getComparatorProfundidade(processamento).setBfs(bfs));
            rankingAtual.clear();
            int i = 0;
            for (i = 0; i < processamento.getOpcoesPossiveisAtuais().size(); i++) {
                Integer val = processamento.getOpcoesPossiveisAtuais().get(i);
                //Vertice já foi testado em ranking anterior, não precisa recalcular
                if (val < lastAdd) {
                    List<Integer> listRankingVal = rankingAtual.get(val);
                    if (listRankingVal == null) {
                        listRankingVal = new ArrayList<>(4);
                        listRankingVal.add(0);
                        listRankingVal.add(0);
                        listRankingVal.add(0);
                        rankingAtual.put(val, listRankingVal);
                    }
                    continue;
                }
                int bfval = bfs[val];
                if (bfval == 4) {//Rankear opção potencial                    
                    List<Integer> listRankingVal = rankingAtual.get(val);
                    if (listRankingVal == null) {
                        listRankingVal = new ArrayList<>(4);
                        //Rankear opção
                        rankingAtual.put(val, listRankingVal);
                    }
                    rankearOpcao(processamento, posicaoAtual, val);
                } 
                //else {
                    //zeraRankOpcao(processamento, posicaoAtual, val);
                //}
                if (processamento.verboseRankingOption) {
                    System.out.printf("Ranking (%4d,%4d): ", processamento.trabalhoAtual, val);
                    UtilProccess.printArray(processamento.bfsRanking.depthcount);

                }
            }
            processamento.getOpcoesPossiveisAtuais().subList(0, i).sort(getComparatorProfundidade(processamento).setMapList(rankingAtual));
        } else {
            List<Integer> subList = null;
            try {
                processamento.getOpcoesPossiveisAtuais().sort(getComparatorProfundidade(processamento).setBfs(bfs));
                subList = processamento.getOpcoesPossiveisAtuais().subList(0, rankingAtual.size());
                subList.sort(getComparatorProfundidade(processamento).setMapList(rankingAtual));
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public void zeraRankOpcao(Processamento processamento, Integer posicaoAtual, Integer val) {
        processamento.bfsRanking(val);
//        processamento.bfsRanking(val);
        List<Integer> listRankingVal = processamento.historicoRanking.get(posicaoAtual).getOrDefault(val, new ArrayList<>(4));
        listRankingVal.clear();

        listRankingVal.add(0);
        listRankingVal.add(0);
        listRankingVal.add(0);
        listRankingVal.add(0);
    }

    @Override
    public void rankearOpcao(Processamento processamento, Integer posicaoAtual, Integer val) {
        processamento.bfsRanking(val);
//        processamento.bfsRanking(val);
        List<Integer> listRankingVal = processamento.historicoRanking.get(posicaoAtual).get(val);
        listRankingVal.clear();

        for (int i = 0; i < processamento.getOpcoesPossiveisAtuais().size(); i++) {
            Integer val2 = processamento.getOpcoesPossiveisAtuais().get(i);
            if (val2 < val) {
                int curval = processamento.bfsRanking.bfs[val2];
                processamento.bfsRanking.depthcount[curval]--;
            }
        }

        listRankingVal.add(processamento.bfsRanking.depthcount[4]);
        listRankingVal.add(-processamento.bfsRanking.depthcount[3]);
        listRankingVal.add(processamento.bfsRanking.depthcount[2]);
    }
}
