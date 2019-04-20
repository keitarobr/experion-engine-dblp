package br.ufsc.ppgcc.experion.extractor.input.engine.dblp;

import br.ufsc.ppgcc.experion.extractor.input.engine.technique.KeygraphExtractionTechniqueTFIDF;

public class KeygraphExtractionTechniqueDBLPTFIDF extends KeygraphExtractionTechniqueTFIDF {

    public KeygraphExtractionTechniqueDBLPTFIDF() {
        super(new KeyGraphDBLP());
    }

}
