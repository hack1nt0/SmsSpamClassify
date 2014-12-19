package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Options;
import com.xiaomi.smsspam.Utils.Statistics;
import com.xiaomi.smsspam.Utils.Tokenizer;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.nio.Buffer;
import java.util.*;

/**
 * Created by dy on 14-10-22.
 */
public class Word extends Rule {

    private static Map<String, Integer> glossary;
    private static Map<String, Integer> reverseGlossary;
    PrintWriter tmpOut;


    private static Tokenizer tokenizer;

    private List<String> curTokens;

    public Word() {
        try {
            tmpOut = new PrintWriter(new FileOutputStream("data/extractedTokens.txt"));
            curTokens = new ArrayList<>();
            tokenizer = new Tokenizer();
            glossary = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    }

    @Override
    public void updRemainingBody(Corpus cps) {
    }

    @Override
    public void process(Corpus cps) {
        curTokens.clear();
        for (String line: cps.getRemainingBody()) {
            String[] segs = tokenizer.cut(line);
            for (String seg : segs) {
                if (Options.ONLY_DICT_WORD && !tokenizer.inDict(seg)) continue;
                curTokens.add(seg);
            }
        }
        cps.setRemainingBody(new ArrayList<>(curTokens));
        cps.setTokens(new ArrayList<>(curTokens));

        tmpOut.println(curTokens);
    }

    @Override
    public int getSubFeatureCnt() {
        return 0;
    }

    @Override
    public String[] getSubFeatureNames() {
        return glossary.keySet().toArray(new String[0]);
    }

    @Override
    public void train(List<Corpus> cpss) {

        /*
        //construct the glossary

        for (Corpus cps: cpss) {
            curTokens.clear();
            for (String segment: cps.getRemainingBody()) {
                String[] segs = tokenizer.cut(segment);
                for (String token : segs) {
                    curTokens.add(token);
                    if (!glossary.containsKey(token)) glossary.put(token, glossary.size());
                }
            }
            cps.setRemainingBody(new ArrayList<>(curTokens));
            cps.setTokens(new ArrayList<>(curTokens));
        }

        long beforeT = System.currentTimeMillis();
        Map<String, Double> word2ig = new HashMap<>();
        for (String t: glossary.keySet()) {
            word2ig.put(t, Statistics.getMI(cpss, t, 0));
            System.out.println(t + "->" + word2ig.get(t));
        }

        long afterT = System.currentTimeMillis();
        System.out.println("word2ig done in " + (afterT - beforeT) / 1000.0 + "s");

        int ratio = 100;

        PriorityQueue<String> pq = new PriorityQueue<>();


        List<String> validTokens = new ArrayList(glossary.keySet());
        Collections.sort(validTokens, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                double a = word2ig.get(o2);
                double b = word2ig.get(o1);
                if (a > b) return 1;
                else if (a < b) return -1;
                else return 0;
            }
        });
        try {
            PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream("data/validTokens.txt")));
            for (String t : validTokens) out.println(t + "\t" + word2ig.get(t));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        glossary.clear();
        for (int i = 0; i < validTokens.size() / ratio; ++i) glossary.put(validTokens.get(i), glossary.size());
        */
        try {
            glossary.clear();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/validTokens.txt")));
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                String[] tmp = line.split("\\t");
                glossary.put(tmp[0], glossary.size());
            }
            in.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean allChinese(String token) {
        for (int i = 0; i < token.length(); ++i)
            if (!isChinese(token.charAt(i))) return false;
        return true;
    }

    public static boolean isChinese(char a) {
        int v = (int)a;
        return (v >=19968 && v <= 171941);
    }
    @Override
    public String toString() {
        return "Word";
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {

    }
}
