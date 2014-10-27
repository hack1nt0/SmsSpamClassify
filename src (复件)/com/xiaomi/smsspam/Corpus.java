package com.xiaomi.smsspam;

import java.util.List;


public class Corpus{

    public boolean isProceeded = false;
	public final static String BODY = "body";
    public final static String SPAM = "spam";
    public final static String ADDRESS = "address";
    
    private List<String> mSegments;
    
    //
    private List<String> mNewSegments;
    //
    
    public List<String> mSplitsAfterRules = null;
    public List<String> mCutPhrases;
    private String mOrigBody;
    private String mCleanBody;

    private boolean mIsSpam;  // true is spam, false is normal
    private int[] mRulesPre;
    private int[] mRules;

    private String mAddress;
    private boolean mMarked = false;
    private boolean mCatChanged = false;
    
    public boolean isCatChanged() {
        return mCatChanged;
    }

    public void setCatChanged(boolean catChanged) {
        this.mCatChanged = catChanged;
    }

    public boolean isMarked() {
        return mMarked;
    }

    public void setMarked(boolean marked) {
        this.mMarked = marked;
    }

    public String getCleanBody() {
        return mCleanBody;
    }

    public void setCleanBody(String cleanBody) {
        this.mCleanBody = cleanBody;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public String getOrigBody() {
        return mOrigBody;
    }

    public void setOrigBody(String origBody) {
        this.mOrigBody = origBody;
    }

    public int[] getRulesPre() {
        return mRulesPre;
    }

    public void setRulesPre(int[] rules) {
        mRulesPre = rules;
    }

    public int[] getRules() {
        return mRules;
    }

    public void setRules(int[] rules) {
        mRules = rules;
    }

    //
    public List<String> getSegments(){
        return mSegments;
    }

    public List<String> getNewSegments(){
        return mNewSegments;
    }

    public boolean isSpam(){
        return mIsSpam;
    }

    public void setSegments(List<String> segs){
        mSegments = segs;
    }
    
    //
    public void setNewSegments(List<String> segs){
    	mNewSegments = segs;
    }
    //

    public void setSpam(boolean isSpam){
        mIsSpam = isSpam;
    }

    public Corpus clone(){
        Corpus cps = new Corpus();
        cps.mAddress = mAddress;
        cps.mIsSpam = mIsSpam;
        cps.mOrigBody = mOrigBody;
        return cps;
    }
}
