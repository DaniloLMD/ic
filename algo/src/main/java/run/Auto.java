package run;

import util.PipeGraph;

/**
 *
 * @author braully
 */
public class Auto {

    /*
    
 -0                             Estagnação de Vertice
 -1                             Evitar colisão
 -2                             Estangar em Bloco
 -3                             Gerar em Bloco Sequencial
 -4                             Estagnar em Bloco Paralelamente
 -5                             Estagnar em Bloco Paralelamente Optimin
 -6                             Estagnação lenta
 -7                             Estagnação de Vertice Crescente
 -c,--continue <arg>            continue process from comb state
 -cf,--commit-fail <arg>        Commit count fail
 -cp,--check-possibility        check possiblities
 -i,--input <arg>               input file graph
 -iparallel,--iparallel <arg>   Parallel process
 -l,--load-start                load start information
 -mc,--merge-continue <arg>     merge multiple continue process from comb
                                state
 -nfi,--not-fail-inviable       Not fail on inviable graph
 -np,--nparallel <arg>          Parallel process
 -o,--output <arg>              output file
 -r,--ranking <arg>             Ranking deepth
 -rf,--rollback-fail <arg>      Rollback count fail
 -rs,--resume                   resume last
 -s,--sanitize                  sanitizar grafo
 -si,--strip-incomplete         Strip incomplete vertices
 -st,--stat                     statitics from graph
 -uc,--uncompress-possibility   compress possiblity list
 -v,--verbose                   verbose processing
 -vi,--verbose-init             verbose initial possibilities
 -vp,--vparallel <arg>          Parallel process
 -vr,--verbose-ranking          verbose ranking
    
     */
    public static void main(String... args) {
        String[] param = args;
        if (param == null || param.length == 0) {
            param = new String[]{"--load-start", "--continue", "", "-7", "-oi"};
            System.out.println("Using default options: --load-start --continue -7");
        }
        PipeGraph.main(param);
    }
}
