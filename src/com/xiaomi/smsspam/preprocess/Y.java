package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by dy on 14-10-27.
 */
public class Y extends Rule {

    @Override
    public void reset() {

    }

    @Override
    public void updRemainingBody(Corpus cps) {

    }

    @Override
    public void process(Corpus cps) {
        cps.getX()[0] = cps.getIsSpam() ? 1 : 0;
    }

    @Override
    public int subClassCount() {
        return 1;
    }

    @Override
    public void train(List<Corpus> cpss) {
    }

    @Override
    public String toString() {
        return "Y";
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {

    }
}
