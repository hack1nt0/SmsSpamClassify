package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.util.ArrayList;
import java.util.List;

public class RuleManager {

    private static int ruleCnt;
    private ArrayList<String> ruleName;
    private RulePrevious[] ruleObjs = {
            new Emoji(),
            new Bracket(),
            new TabsSerial(),
            new Url(),
            new Numbers(),
            new Word(),
            //new SpecificSymbol(),
            //new NewPhrase(),
            //new SmsLength()

    };
    private int[] startIndex;

    public RuleManager(){
        ruleName = new ArrayList<String>();
        ruleCnt = 0;
        startIndex = new int[ruleObjs.length];
        for(int i = 0; i < ruleObjs.length; ++i){
            int subCount = ruleObjs[i].subClassCount();
            for(int j = 0; j < subCount; ++j){
                ruleName.add(ruleObjs[i].getClassName(j));
            }
            startIndex[i] = ruleCnt;
            ruleCnt += subCount;
        }
    }

    public RulePrevious[] getRuleObjs() {
        return ruleObjs;
    }

    public static int getRuleCnt(){
        return ruleCnt;
    }

    // the rules need to be re-train on the cpss
    public void process(List<Corpus> cpss){
        for(int i = 0; i < ruleObjs.length; ++i){
            ruleObjs[i].reset();
            ruleObjs[i].train(cpss);
            for (Corpus cps: cpss) {
                hasRulesPreHist(cps);
                //upd segments
                cps.setRefinedSegments(ruleObjs[i].process(cps.getRefinedSegments()));
                //upd ruleHits
                ruleObjs[i].fit(cps, startIndex[i]);
            }
        }
    }

    private void hasRulesPreHist(Corpus cps) {
        if (cps.getRulesPreHits() == null) cps.setRulesPreHits(new int[getRuleCnt()]);
    }

    //no need
    public void process(Corpus cps) {
        hasRulesPreHist(cps);
        for(int i = 0; i < ruleObjs.length; ++i){
            //upd segments
            cps.setRefinedSegments(ruleObjs[i].process(cps.getRefinedSegments()));
            //upd ruleHits
            ruleObjs[i].fit(cps, startIndex[i]);
        }
    }

}
