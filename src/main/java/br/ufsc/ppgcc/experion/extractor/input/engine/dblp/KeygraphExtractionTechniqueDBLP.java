package br.ufsc.ppgcc.experion.extractor.input.engine.dblp;

import br.ufsc.ppgcc.experion.extractor.input.engine.technique.KeygraphExtractionTechnique;

public class KeygraphExtractionTechniqueDBLP extends KeygraphExtractionTechnique {

    public KeygraphExtractionTechniqueDBLP() {
        super(new KeyGraphDBLP());
    }

}
