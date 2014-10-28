package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Options;
import com.xiaomi.smsspam.Utils.Tokenizer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public Word() {
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
        glossary.clear();
    }

    @Override
    public void process(Corpus cps) {
        List<String> tokens = new ArrayList<>();
        for (String line: cps.getRefinedSegments()) {
            String[] segs = tokenizer.cut(line);
            for (String seg : segs) {
                if (Options.ONLY_DICT_WORD && !tokenizer.inDict(seg)) continue;
                tokens.add(seg);
            }
        }
        cps.setRefinedSegments(tokens);
        cps.setTokens(tokens);//TODO
    }

    //to tokenize on a whole sms, not applied now

    @Override
    public int subClassCount() {
        return 0;
    }

    @Override
    public void train(List<Corpus> cpss) {
        for (Corpus cps : cpss) {
            for (String seg : cps.getRefinedSegments()) {
                String[] newSegs = tokenizer.cut(seg);
                for (String nseg : newSegs) {
                    if (Options.ONLY_DICT_WORD && !tokenizer.inDict(nseg)) continue;
                    if (!glossary.containsKey(nseg)) glossary.put(nseg, glossary.size());
                }
            }
        }
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
