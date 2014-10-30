package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Options;
import com.xiaomi.smsspam.Utils.Tokenizer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dy on 14-10-22.
 */
public class Word extends RulePrevious{

    private static Map<String, Integer> glossary;

    private static Tokenizer tokenizer;

    private List<String> curTokens;

    public Word() {
        curTokens = new ArrayList<>();
        try {
            extractedRulesOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/extractedTokens.txt")));
            modelOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/validTokens.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        tokenizer = new Tokenizer();
        glossary = new HashMap<>();
    }

    public static Tokenizer getSeg(){
        return tokenizer;
    }

    public static Map<String, Integer> getGlossary() {
        return glossary;
    }

    @Override
    public void reset() {
        curTokens.clear();
        glossary.clear();
    }

    @Override
    public void updRemainingBody(Corpus cps) {
        curTokens.clear();
        for (String line: cps.getRemainingBody()) {
            String[] segs = tokenizer.cut(line);
            for (String seg : segs) {
                if (Options.ONLY_DICT_WORD && !tokenizer.inDict(seg)) continue;
                curTokens.add(seg);
            }
        }
        cps.setRemainingBody(new ArrayList<>(curTokens));
    }

    @Override
    public void process(Corpus cps) {
        updRemainingBody(cps);
        writeCurRules(extractedRulesOut, curTokens);
        cps.setTokens(cps.getRemainingBody());//TODO
    }

    //to tokenize on a whole sms, not applied now

    @Override
    public int subClassCount() {
        return 0;
    }

    @Override
    public void train(List<Corpus> cpss) {
        //construct the glossary
        for (Corpus cps: cpss) {
            updRemainingBody(cps);
            for (String token: curTokens)
                if (!glossary.containsKey(token)) glossary.put(token, glossary.size());
        }
        writeCurRules(modelOut, glossary.keySet());
    }

    @Override
    public String getName() {
        return "Word";
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {

    }
}
