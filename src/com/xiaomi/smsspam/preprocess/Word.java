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

    private List<String> tokens;

    public Word() {
        tokenizer = new Tokenizer();
        glossary = new HashMap<>();
        tokens = new ArrayList<>();
    }

    public static Tokenizer getSeg(){
        return tokenizer;
    }

    public static Map<String, Integer> getGlossary() {
        return glossary;
    }

    @Override
    public void reset() {
        tokens.clear();
    }

    @Override
    public boolean fit(Corpus cps, int startIndex) {
        cps.setSegments(new ArrayList<>(tokens));
        return cps.getSegments().size() > 0;
    }

    @Override
    protected List<String> process(String str) {
        List<String> res = new ArrayList<>();
        String[] segs = tokenizer.cut(str);
        for (String seg : segs) {
            if (Options.ONLY_DICT_WORD && !tokenizer.inDict(seg)) continue;
            tokens.add(seg);
            res.add(seg);
        }
        return res;
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
