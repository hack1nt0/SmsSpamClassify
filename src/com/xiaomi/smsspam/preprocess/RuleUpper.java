package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class RuleUpper extends Rule {

	public boolean fit(Corpus cps, int[] vals, int start){
        if(null == cps || null == vals || start >= vals.length){
            return false;
        }
        return doFitting(cps,  vals, start);
    }

    protected abstract boolean doFitting(Corpus cps, int[] vals, int start);

    // do some things after all corpus handled by RulePrevious, if need
    public void firstStepDone(){
        System.out.println("Symbol done 1 0 0  11111" );}

    // compute every corpus after RulePrevious, if need
    // It's only should be called on training step.
    protected void doFirstStep(Corpus cps){}

    @Override
    public void train(List<Corpus> cpss) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {

    }
}
