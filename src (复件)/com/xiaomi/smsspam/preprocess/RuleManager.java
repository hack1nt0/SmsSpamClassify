package com.xiaomi.smsspam.preprocess;

import java.util.ArrayList;
import java.util.List;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Options;
import com.xiaomi.smsspam.Utils.Tokenizer;
import com.xiaomi.smsspam.Utils.SimpleDict;

public class RuleManager {
	
	private static Tokenizer mSeg;
	public static NewSegment mNewSeg;
	

    static{
        mSeg = new Tokenizer();
        mSeg.init();
    }
    
    static{
    	mNewSeg = new NewSegment();
    	//mNewSeg.init();
    }

    public static Tokenizer getSeg(){
        return mSeg;
    }

    public RuleManager() {
    }
    
    public RuleManager(RulePrevious[] rp,RuleUpper[] ru)
    {
    	mRulesPrevious = rp;
    	mRulesUpper = ru;
    }

    private int mRulePreCount;
    private int mRuleUpperCount;
    private ArrayList<String> mNames;
    public RulePrevious[] mRulesPrevious = {
                                            new TabsSerial(),
                                            new TabsInsert(),
                                            new Url(),
                                            new Numbers(),
                            };
    private int[] mPreStartIndex;

    public RuleUpper[] mRulesUpper ={
                                        new SpecificSymbol(),
                                        new SmsLength()
    };
    private int[] mUpperStartIndex;

    public String getRuleName(int index){
        return mNames.get(index);
    }

    private boolean mPreInit = false;
    public void initPreRules(){
        if(mPreInit){
            return;
        }else{
            mPreInit = true;
        }
        mNames = new ArrayList<String>();
        mRulePreCount = 0;

        mPreStartIndex = new int[mRulesPrevious.length];
        for(int i = 0; i < mRulesPrevious.length; ++i){
            int subCount = mRulesPrevious[i].subClassCount();
            for(int j = 0; j < subCount; ++j){
                mNames.add(mRulesPrevious[i].getClassName(j));
            }
            mPreStartIndex[i] = mRulePreCount;
            mRulePreCount += subCount;
        }
    }

    private boolean mUpperInit = false;
    public void initUpperRules(){
        if(mUpperInit){
            return;
        }else{
            mUpperInit = true;
        }
        
        firstStepDone();

        mRuleUpperCount = 0;

        mUpperStartIndex = new int[mRulesUpper.length];
        for(int i = 0; i < mRulesUpper.length; ++i){
            int subCount = mRulesUpper[i].subClassCount();
            for(int j = 0; j < subCount; ++j){
                mNames.add(mRulesUpper[i].getClassName(j));
            }
            mUpperStartIndex[i] = mRuleUpperCount;
            mRuleUpperCount += subCount;
        }
    }

    public void process(List<Corpus> cpses){
        for(Corpus cps : cpses)
            process(cps);
    }
    
    public void process(Corpus cps){

        initPreRules();
        doRulesPrevious(cps);

        // Need to do some statics if upper rules haven't been initialized
        if(!mUpperInit){
            firstStepUpper(cps);
        }

        if(!Options.TEST_RULES){
            if(!mNewSeg.mInit)
            {
                List<String> segsList = new ArrayList<String>();
                for(String split : cps.mSplitsAfterRules){
                    String[] segs = mSeg.cut(split);
                    for(String seg : segs){
                        if(!Options.ONLY_DICT_WORD || mSeg.inDict(seg)){
                            segsList.add(seg);
                        }
                    }
                }
                cps.setSegments(segsList);
            }

            //
            if(mNewSeg.mInit)
            {
                List<String> segsListNew = new ArrayList<String>();
                for(String split : cps.mSplitsAfterRules)
                {
                    List<String> segs = mNewSeg.cut(split);
                    for(String seg : segs)
                    {
                        segsListNew.add(seg);
                    }
                }
                cps.setSegments(segsListNew);
            }
            //
        }

        initUpperRules();
        doRulesUpper(cps);

        cps.isProceeded = true;

    }

    private void firstStepDone(){
        for(int i = 0; i < mRulesUpper.length; ++i){
            mRulesUpper[i].firstStepDone();
        }
    }

    private void firstStepUpper(Corpus cps){
        
        for(int i = 0; i < mRulesUpper.length; ++i){
            mRulesUpper[i].doFirstStep(cps);
        }
    }

    private void doRulesPrevious(Corpus cps){
        List<String> splits = new ArrayList<String>();
        splits.add(cps.getOriginBody());
        for(RulePrevious rule : mRulesPrevious){
            rule.reset();
        }
        int[] ruleHits = new int[mRulePreCount];
        for(int i = 0; i < mRulesPrevious.length; ++i){
            splits = mRulesPrevious[i].process(splits);
            mRulesPrevious[i].fit(ruleHits, mPreStartIndex[i]);
        }
        cps.setRulesPreHits(ruleHits);
        cps.mSplitsAfterRules = splits;
    }

    // Merge previous rules into upper rules
    private void doRulesUpper(Corpus cps){
        int[] ruleHits = new int[getRuleCount()];
        int[] pre = cps.getRulesPreHits();
        for(int i = 0; i < mRulePreCount; ++i){
            ruleHits[i] = pre[i];
        }
        for(int i = 0; i < mRulesUpper.length; ++i){
            mRulesUpper[i].fit(cps, ruleHits, mRulePreCount + mUpperStartIndex[i]);
        }
        cps.setRules(ruleHits);
    }

    public int getRuleCount(){
        if(mPreInit && mUpperInit){
            return mRuleUpperCount + mRulePreCount;
        }else{
            return 0;
        }
    }

    public void process(List<Corpus> cpses, SimpleDict dict){
        for(Corpus cps : cpses){
            List<String> splits = cps.mSplitsAfterRules;
            if(splits == null){
                doRulesPrevious(cps);
            }

            List<String> segsList = new ArrayList<String>();
            List<String> segsListNew = new ArrayList<String>();
            List<String> segNewPhraseList = new ArrayList<String>();
            List<String> segNewPhraseListNew = new ArrayList<String>();
            
            
            for(String split : cps.mSplitsAfterRules){
                List<String>[] cuts = dict.cut(split);
                for(String normalSplit : cuts[SimpleDict.NORMAL]){
                    String[] segs = mSeg.cut(normalSplit);
                    for(String seg : segs){
                        if(!Options.ONLY_DICT_WORD || mSeg.inDict(seg)){
                            segsList.add(seg);
                        }
                    }
                }
                segNewPhraseList.addAll(cuts[SimpleDict.NEW]);
            }
            
            
            for(String split : cps.mSplitsAfterRules){
                List<String>[] cuts = dict.cut(split);
                for(String normalSplit : cuts[SimpleDict.NORMAL]){
                    List<String> segs = mNewSeg.cut(normalSplit);
                    for(String seg : segs){
                        if(!Options.ONLY_DICT_WORD || mSeg.inDict(seg)){
                            segsListNew.add(seg);
                        }
                    }
                }
                
                segNewPhraseListNew.addAll(cuts[SimpleDict.NEW]);
            }
            
            

            doRulesUpper(cps);
//            System.out.println("----------cps.Orig:" + cps.getOriginBody());
//            System.out.print("segNewPhraseList:");
//            for(String s : segNewPhraseList){
//                System.out.print(s + ", ");
//            }
//            System.out.println();
//            System.out.println("segsList:");
//            for(String s : segsList){
//                System.out.println(s + ", ");
//            }
            
            cps.setSegments(segsList);
            cps.mCutPhrases = segNewPhraseList;
            if(mNewSeg.mInit)
            {
            	cps.setSegments(segsListNew);
            	cps.mCutPhrases = segNewPhraseListNew;
            }
            
        }
    }
    
}
