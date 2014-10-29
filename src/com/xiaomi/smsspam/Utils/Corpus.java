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
    private List<String> refinedSegments;
    private String originBody;
    private boolean isSpam;
    private int[] X;
    private String address;

    public Corpus(String originBody, boolean isSpam, String address) {
        this.originBody = originBody;
        this.refinedSegments = new ArrayList<>(Arrays.asList(originBody));
        this.isSpam = isSpam;
        this.address = address;
        this.X = new int[2];
    }

    public void reset() {
        this.refinedSegments = new ArrayList<String>(Arrays.asList(originBody));
        this.tokens = null;
    }

    public void clear(){
        tokens = refinedSegments = null;
        X = null;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOriginBody() {
        return originBody;
    }

    public void setOriginBody(String origBody) {
        this.originBody = origBody;
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

    public List<String> getRefinedSegments(){
        return refinedSegments;
    }

    public void setRefinedSegments(List<String> segs){
        refinedSegments = segs;
    }

    public boolean getIsSpam(){
        return isSpam;
    }

    public void setIsSpam(boolean isSpam){
        this.isSpam = isSpam;
    }

    public Corpus clone(){
        return new Corpus(originBody, isSpam, address);
    }
}
