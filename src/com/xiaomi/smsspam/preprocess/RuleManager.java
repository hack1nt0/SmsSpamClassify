package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.util.ArrayList;
import java.util.List;

public class RuleManager {

    private int ruleCnt;
    private static Rule[] rules = {
            new Y(),
            new NER(),
           //new Bracket(),
            new Splits(),
            //new Emoji(),
            new Word(),
            //new Url(),
            //new Numbers(),
            //new SpecificSymbol(),
            //new NewPhrase(),
            //new SmsLength(),
    };

    public static List<String> getRuleNames() {
        List<String> ret = new ArrayList<>();
        for (Rule r: rules) {
            if (r instanceof Word) continue;
            for (String n : r.getSubFeatureNames()) ret.add(n);
        }
        return ret;
    }

    public int getFeatureCnt() {
        return ruleCnt;
    }

    public RuleManager(){
        for(int i = 0; i < rules.length; ++i){
            rules[i].reset();
            rules[i].setStartIndex(ruleCnt);
            ruleCnt += rules[i].getSubFeatureCnt();
        }
    }

    public Rule[] getRules() {
        return rules;
    }

    public int getRuleCnt(){
        return ruleCnt;
    }

    // the rules need to be re-train on the cpss
    public void train(List<Corpus> cpss){
        for(int i = 0; i < rules.length; ++i){
            rules[i].reset();
            rules[i].train(cpss);
            //rules[i].setStartIndex(ruleCnt);
            //ruleCnt += rules[i].getSubFeatureCnt();
        }
    }

    public void process(Corpus cps) {
        cps.reset();
        cps.setX(new int[getRuleCnt()]);//TODO
        for (int i = 0; i < rules.length; ++i) {
            rules[i].reset();
            rules[i].process(cps);
        }
    }

    public void process(List<Corpus> cpss) {
        for (Corpus cps: cpss) process(cps);
    }

}
