package com.xiaomi.smsspam.Utils;

import java.util.List;


public class Corpus{
    public final static boolean isProceeded = false;
    public final static String BODY = "body";
    public final static String SPAM = "spam";
    public final static String ADDRESS = "address";

    //segments after tokenizer(after some rules such as emoji and url)
    private List<String> segments;

    //segments after all rules
    private List<String> refinedSegments;

    private String originBody;
    private String refinedBody;

    private boolean isSpam;
    private int[] rulesPreHits;

    private String address;
    private boolean marked = false;
    private boolean catalogChanged = false;
    
    public boolean getCatChanged() {
        return catalogChanged;
    }

    public void setCatChanged(boolean catChanged) {
        this.catalogChanged = catChanged;
    }

    public boolean getMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public String getRefinedBody() {
        return refinedBody;
    }

    public void setRefinedBody(String cleanBody) {
        this.refinedBody = cleanBody;
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

    public int[] getRulesPreHits() {
        return rulesPreHits;
    }

    public void setRulesPreHits(int[] preRuleHits) {
        rulesPreHits = preRuleHits;
    }

    public List<String> getSegments(){
        return segments;
    }

    public void setSegments(List<String> segs){
        segments = segs;
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
        Corpus cps = new Corpus();
        cps.address = address;
        cps.isSpam = isSpam;
        cps.originBody = originBody;
        return cps;
    }
}
