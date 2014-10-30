package com.xiaomi.smsspam.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Corpus{
    public final static String BODY = "body";
    public final static String SPAM = "spam";
    public final static String ADDRESS = "address";
    //tokens after tokenizer(after some rules such as emoji and url)
    private List<String> tokens;
    //tokens after all rules
    private List<String> remainingBody;
    private String originalBody;
    private boolean isSpam;
    private int[] X;
    private String address;

    public Corpus(String originalBody, boolean isSpam, String address) {
        this.originalBody = originalBody;
        this.remainingBody = new ArrayList<>(Arrays.asList(originalBody));
        this.isSpam = isSpam;
        this.address = address;
        this.X = new int[2];
    }

    public void reset() {
        this.remainingBody = new ArrayList<String>(Arrays.asList(originalBody));
        this.tokens = null;
    }

    public void clear(){
        tokens = remainingBody = null;
        X = null;
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
}
