package br.ufsc.ppgcc.experion.extractor.input.engine.dblp;

import br.ufsc.ppgcc.experion.extractor.algorithm.keygraph.Keygraph;

import java.io.InputStream;
import java.net.URL;

public class KeyGraphDBLP extends Keygraph {
    @Override
    public InputStream getConfig() {
        return this.getClass().getResourceAsStream("/DBLPConstants.txt");
    }
}
