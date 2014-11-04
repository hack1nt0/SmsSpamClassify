package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.util.List;

public class RuleManager {

    private static int ruleCnt;
    private RulePrevious[] ruleObjs = {
            new Y(),
            //new Bracket(),
            //new Splits(),
            //new Url(),
            //new Numbers(),
            //new Emoji(),
            new Word(),
            //new SpecificSymbol(),
            //new NewPhrase(),
            //new SmsLength(),
    };

    public RuleManager(){
    }

    public RulePrevious[] getRuleObjs() {
        return ruleObjs;
    }

    public static int getRuleCnt(){
        return ruleCnt;
    }

    // the rules need to be re-train on the cpss
    public void train(List<Corpus> cpss){
        for(int i = 0; i < ruleObjs.length; ++i){
            ruleObjs[i].reset();
            ruleObjs[i].train(cpss);
            ruleObjs[i].setStartIndex(ruleCnt);
            ruleCnt += ruleObjs[i].subClassCount();
        }
    }

    public void process(Corpus cps) {
        cps.setX(new int[getRuleCnt()]);//TODO
        for (int i = 0; i < ruleObjs.length; ++i) {
            ruleObjs[i].process(cps);
        }
    }

    public void process(List<Corpus> cpss) {
        for (Corpus cps: cpss) {
            cps.reset();
            process(cps);
        }
    }

}
