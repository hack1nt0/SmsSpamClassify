package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dy on 14-10-27.
 */
public class Y extends RulePrevious {

    @Override
    public void reset() {

    }

    @Override
    public boolean fit(Corpus cps, int startIndex) {
        cps.getRulesPreHits()[startIndex] = cps.getIsSpam() ? 1 : 0;
        return cps.getIsSpam();
    }

    @Override
    protected List<String> process(String str) {
        return new ArrayList<>(Arrays.asList(str));
    }

    @Override
    public int subClassCount() {
        return 1;
    }

    @Override
    public void train(List<Corpus> cpss) {

    }

    @Override
    public String getName() {
        return "Y";
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {

    }
}
