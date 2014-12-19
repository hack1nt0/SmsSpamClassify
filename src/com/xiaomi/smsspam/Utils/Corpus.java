package com.xiaomi.smsspam.Utils;

import com.xiaomi.smsspam.MainClass;
import com.xiaomi.smsspam.preprocess.Rule;
import com.xiaomi.smsspam.preprocess.RuleManager;
import com.xiaomi.smsspam.preprocess.Word;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.util.*;


public class Corpus{
    public final static String BODY = "body";
    public final static String SPAM = "spam";
    public final static String ADDRESS = "address";
    //tokens after tokenizer(after some rules such as emoji and url)
    private List<String> tokens;
    private Set<String> uniqTokens;
    //tokens after all rules
    private List<String> remainingBody;
    private String originalBody;
    private boolean isSpam;
    private int[] X;
    private String address;

    public Corpus(String originalBody, boolean isSpam, String address) {
        this.originalBody = originalBody;
        this.remainingBody = new ArrayList<>(Arrays.asList(originalBody));
        tokens = new ArrayList<>();
        uniqTokens = new HashSet<>();
        this.isSpam = isSpam;
        this.address = address;
        this.X = new int[1];
    }

    public void reset() {
        this.remainingBody = new ArrayList<String>(Arrays.asList(originalBody));
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOriginalBody() {
        return originalBody;
    }

    public void setOriginalBody(String origBody) {
        this.originalBody = origBody;
    }

    public int[] getX() {
        return X;
    }

    public void setX(int[] X) {
        this.X = X;
    }

    public List<String> getTokens(){
        return tokens;
    }

    public void setTokens(List<String> segs){
        tokens = segs;
        for (String t: tokens) uniqTokens.add(t);
    }

    public boolean containsToken(String token) {
        return uniqTokens.contains(token);
    }

    public List<String> getRemainingBody(){
        return remainingBody;
    }

    public void setRemainingBody(List<String> segs){
        remainingBody = segs;
    }

    public boolean getIsSpam(){
        return isSpam;
    }

    public void setIsSpam(boolean isSpam){
        this.isSpam = isSpam;
    }

    public Corpus clone(){
        return new Corpus(originalBody, isSpam, address);
    }

    public static void main(String[] args) throws IOException{
        PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream("data/01Matrix.tab")));
        List<Corpus> trainData = MainClass.readSMS("data/all_processed.txt.dist.filtled");
        RuleManager ruleManager = new RuleManager();
        ruleManager.train(trainData);
        ruleManager.process(trainData);

        Rule[] rules = ruleManager.getRules();
        for (int i = 0; i < rules.length; ++i) {
            String[] subFeatureNames = rules[i].getSubFeatureNames();
            for (int j = 0; j < subFeatureNames.length; ++j) out.print(subFeatureNames[j] + "\t");
        }
        out.println();
        int[] X = new int[ruleManager.getFeatureCnt() + Word.getGlossary().size()];
        for (int i = 0; i < X.length; ++i) out.print("d\t"); out.println();
        out.println("class\t");

        for (Corpus cps: trainData) {
            Arrays.fill(X, 0);
            for (int i = 0; i < rules.length; ++i) {
                if (rules[i] instanceof Word) {
                    for (String token : cps.getTokens())
                        if (Word.getGlossary().containsKey(token))
                            X[ruleManager.getFeatureCnt() + Word.getGlossary().get(token)] = 1;
                    continue;
                }
                for (int j = rules[i].getStartIndex(); j < rules[i].getSubFeatureCnt(); ++j)
                    X[j] = cps.getX()[j] > 0 ? 1 : 0;
            }
            for (int i = 0; i < X.length; ++i) out.print(X[i] + "\t");
            out.println();
            System.out.println(Arrays.asList(X));
        }
        out.close();

       //BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/01Matrix.cab")));

    }
}
