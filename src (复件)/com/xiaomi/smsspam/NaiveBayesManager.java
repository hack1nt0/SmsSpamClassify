package com.xiaomi.smsspam;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.SimpleDict;
import com.xiaomi.smsspam.Utils.Utils;
import com.xiaomi.smsspam.preprocess.NewPhrase;
import com.xiaomi.smsspam.preprocess.RuleManager;

public class NaiveBayesManager {

	private Map<String, Integer> termIndex;
    private Map<String, Double> mTermIGs;
    private List<String> mTerms;
    private List<Integer> mMaxIGsIndex;

    private int                  spamNormalCount[];

    private int                  classKeyMap[][];

    private int                  mRuleCountMap[][];

    private int                  vocabulary      = 0;

    private int                  totalTermsCount = 0;

    private RuleManager mRuleManager;
    public NewPhrase mNewPhrases = new NewPhrase();
    private int mNumNewPhrase = 0;
    private int                  mNewPhraseCountMap[][];
    private Map<String, Integer> mNewPhraseIndex;
    private SimpleDict mNewPhraseDict;

    public static final int SPAM = Utils.SPAM;
    public static final int NORMAL = Utils.NORMAL;
    public static final int CLASS_COUNT = Utils.CLASS_COUNT;
    
    //分类器
    NaiveBayes01 nbc = new NaiveBayes01();

    public Set<String> getNewPhrases(){
        return mNewPhraseIndex.keySet();
    }

    public SimpleDict getNewPhraseDict(){
        return mNewPhraseDict;
    }

    private void buildIndex(List<Corpus> orignCorpus) {
        Integer idTerm = new Integer(-1);
        for (int i = 0; i < orignCorpus.size(); ++i) {
            Corpus corpus = orignCorpus.get(i);
            List<String> terms = corpus.getSegments();
            //List<String> terms = corpus.getSegments();

            for (String term : terms) {
                totalTermsCount++;
                if (!termIndex.containsKey(term)) {
                    idTerm++;
                    termIndex.put(term, idTerm);
                    mTerms.add(term);
                }
            }
        }
        vocabulary = termIndex.size();
    }

    private void trainByTerms(List<Corpus> trianCorpus){
        vocabulary      = 0;
        totalTermsCount = 0;
        mTerms = new ArrayList<String>();
        mMaxIGsIndex = new ArrayList<Integer>();
        termIndex = new HashMap<String, Integer>();

        buildIndex(trianCorpus);

        spamNormalCount = new int[CLASS_COUNT];
        classKeyMap = new int[CLASS_COUNT][vocabulary];
        mRuleCountMap = new int[CLASS_COUNT][mRuleManager.getRuleCount()];

        int spamCount = 0;

        for (int i = 0; i < trianCorpus.size(); ++i) {
            Corpus corpus = trianCorpus.get(i);
            boolean isSpam = corpus.getIsSpam();
            if(isSpam){
                spamCount++;
            }
            List<String> terms = corpus.getSegments();
            //List<String> terms = corpus.getSegments();
            int spamIndex = isSpam ? SPAM : NORMAL;

            for(int j = 0; j < terms.size(); ++j){
                String term = terms.get(j);
                boolean duplicated = false;
                for(int k = 0; k < j; ++k){
                    if(term.equals(terms.get(k))){
                        duplicated = true;
                        break;
                    }
                }
                if(duplicated){
                    continue;
                }
                Integer wordIndex = termIndex.get(term);
                classKeyMap[spamIndex][wordIndex]++;
            }
            spamNormalCount[spamIndex]++;
            int[] ruleCounts = corpus.getRules();
            for(int j = 0; j < ruleCounts.length; ++j){
                if(ruleCounts[j] > 0){
                    mRuleCountMap[spamIndex][j]++;
                }
            }
        }

        int total = trianCorpus.size();

        double entropySpam = Utils.getEntropy(total, spamCount);

        List<Pair> infoGains = new ArrayList<Pair>();
        double[] igs = new double[vocabulary];

        if(!Options.TEST_ALG){
            for(int i = 0; i < mRuleManager.getRuleCount(); ++i){
                int hasCount = 0;
                for(int j = 0; j < CLASS_COUNT; ++j){
                    hasCount += mRuleCountMap[j][i];
                }
                int noCount = total - hasCount;
                double entropy = 0.0;
                if(hasCount > 0 && noCount > 0){
                    entropy = entropySpam - (1.0 * hasCount / total) * Utils.getEntropy(hasCount, mRuleCountMap[0][i])
                            - (1.0 * noCount / total) * Utils.getEntropy(noCount, spamCount - mRuleCountMap[0][i]);
                }
                System.out.println(mRuleManager.getRuleName(i) + ":\t" + entropy  + ",\t" + mRuleCountMap[0][i] + ",\t" + mRuleCountMap[1][i]);
            }
        }

        mTermIGs = new HashMap<String, Double>();
        for(int i = 0; i < vocabulary; ++i){
            int hasCount = 0;
            for(int j = 0; j < CLASS_COUNT; ++j){
                hasCount += classKeyMap[j][i];
            }
            
            int noCount = total - hasCount;
            double entropy = 0.0;
            if(hasCount > 0 && noCount > 0){
                entropy = entropySpam - (1.0 * hasCount / total) * Utils.getEntropy(hasCount, classKeyMap[0][i])
                        - (1.0 * noCount / total) * Utils.getEntropy(noCount, spamCount - classKeyMap[0][i]);
            }
            infoGains.add(new Pair(i, entropy));
            igs[i] = entropy;

            mTermIGs.put(mTerms.get(i), entropy);
        }


        Collections.sort(infoGains, new Comparator<Pair>(){
            public int compare(Pair arg0, Pair arg1) {
                return Double.compare(arg1.p, arg0.p);
            }
        });

        mMaxIGsIndex.clear();
        int index = 0;
        while(mMaxIGsIndex.size() < Options.WORD_COUNT_BY_IG && index < infoGains.size()){
            mMaxIGsIndex.add(Integer.valueOf(infoGains.get(index).i));
            index++;
        }

        if(!Options.TEST_ALG){
            System.out.println("----------------------Max " + Options.WORD_COUNT_BY_IG + " information gains start------------------------");
            int count = 0;
            for(int i : mMaxIGsIndex){
                System.out.println(mTerms.get(i) + "," + igs[i] + "," + classKeyMap[0][i] + "," + classKeyMap[1][i]);
                if(count++ > 1300)break;
            }
            System.out.println("----------------------Max " + Options.WORD_COUNT_BY_IG + " information gains end------------------------");
        }
        
    }

    public void startTraining(List<Corpus> orignCorpus, RuleManager rmgr)  {
        mRuleManager = rmgr;

        rmgr.process(orignCorpus);
        trainByTerms(orignCorpus);

        MainClass.log("Step 1");

        mNewPhrases.doExploring(orignCorpus);
        mNewPhrases.checkSubWord(mMaxIGsIndex, mTerms);
        mNumNewPhrase = mNewPhrases.getPhraseCount();
        mNewPhraseCountMap = new int[CLASS_COUNT][mNumNewPhrase];
        mNewPhraseIndex = mNewPhrases.getPhraseMap();
        mNewPhraseDict = mNewPhrases.getDict();

        MainClass.log("Step 2");

        rmgr.process(orignCorpus, mNewPhraseDict);

        MainClass.log("Step 3");

        trainByTerms(orignCorpus);

        MainClass.log("Step 4");
        
        List<String> keyTerms = new ArrayList<String>();
        for(int termIndex : mMaxIGsIndex){String term = mTerms.get(termIndex);keyTerms.add(term);};
        Iterator<Map.Entry<String, Integer>> it = mNewPhraseIndex.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, Integer> entry = it.next();
            keyTerms.add(entry.getKey());
        }
        Collections.sort(keyTerms);
        
        try {
			FileOutputStream dictOut = new FileOutputStream(Options.NEW_DICT);
			for(String term:keyTerms)
			{
				dictOut.write((term+"\n").getBytes());
			}
			dictOut.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        RuleManager.mNewSeg.init(keyTerms);
        
        rmgr.process(orignCorpus);
        trainByTerms(orignCorpus);
//
        MainClass.log("Step 1");
//
//        mNewPhrases.doExploring(orignCorpus);
//        mNewPhrases.checkSubWord(mMaxIGsIndex, mTerms);
//        mNumNewPhrase = mNewPhrases.getPhraseCount();
//        mNewPhraseCountMap = new int[CLASS_COUNT][mNumNewPhrase];
//        mNewPhraseIndex = mNewPhrases.getPhraseMap();
//        mNewPhraseDict = mNewPhrases.getDict();
//
//        MainClass.log("Step 2");
//
//        rmgr.process(orignCorpus, mNewPhraseDict);

//        for (int i = 0; i < orignCorpus.size(); ++i) {
//            Corpus corpus = orignCorpus.get(i);
//            int spamIndex = corpus.getIsSpam() ? SPAM : NORMAL;
//            it = mNewPhraseIndex.entrySet().iterator();
//            while(it.hasNext()){
//                Map.Entry<String, Integer> entry = it.next();
//                if(corpus.mCutPhrases.contains(entry.getKey())){
//                    mNewPhraseCountMap[spamIndex][entry.getValue().intValue()]++;
//                }
//            }
//        }
//
//        MainClass.log("Step 3");
//
//        trainByTerms(orignCorpus);
//
//        MainClass.log("Step 4");
               
        //根据训练结果构造NaiveBayesClassifier nbc，之后会将其存储，作为模型文件     
        
        Collections.sort(mTerms);
        nbc.termProbMap = new double[CLASS_COUNT][mTerms.size()];
        nbc.rmgr = rmgr;
        nbc.ruleProbMap = new double[CLASS_COUNT][rmgr.getRuleCount()];
        //nbc.phraseProbMap = new double[CLASS_COUNT][mNewPhraseIndex.size()];
        nbc.classPreProbMap = new double[CLASS_COUNT];
        
        nbc.termProbLogMap = new double[CLASS_COUNT][mTerms.size()][2];
        nbc.ruleProbLogMap = new double[CLASS_COUNT][rmgr.getRuleCount()][2];
        nbc.classPreProbLogMap = new double[CLASS_COUNT];
        
        for(int i = 0; i < CLASS_COUNT; i++)
        {
        	double p = getPreProbability(i);
        	nbc.classPreProbMap[i] = p;
        	nbc.classPreProbLogMap[i] = Math.log(p);
        }
        //for(int termIndex : mMaxIGsIndex){String term = mTerms.get(termIndex);nbc.termDict.add(term);};
        
        nbc.termDict.addAll(mTerms);

//        it = mNewPhraseIndex.entrySet().iterator();
//        while(it.hasNext()){
//            Map.Entry<String, Integer> entry = it.next();
//            nbc.phraseDict.add(entry.getKey());
//        }
        Collections.sort(nbc.termDict);
//        Collections.sort(nbc.phraseDict);
        for (int cIndex = 0; cIndex < CLASS_COUNT; ++cIndex) {
    		int i = 0;
            for(String term:nbc.termDict){
                
            		int termI = termIndex.get(term);
            		double p = getClassConditionlProbability(cIndex, termI,false);
                   nbc.termProbMap[cIndex][i] = p;
                   nbc.termProbLogMap[cIndex][i][0] = Math.log(p);
                   nbc.termProbLogMap[cIndex][i][1] = Math.log(1-p);
                   
                    i++;                
            }

            for(int j = 0; j < rmgr.getRuleCount(); ++j){
            	double p = getclassRuleProbability(cIndex, j, false);
               nbc.ruleProbMap[cIndex][j] = p;
               nbc.ruleProbLogMap[cIndex][j][0] = Math.log(p);
               nbc.ruleProbLogMap[cIndex][j][1] = Math.log(1-p);
             }
//            if(Options.USE_NEW_PHRASE){
//            	i = 0;
//            	for(String phrase:nbc.phraseDict){
//                    
//            		int phraseI = mNewPhraseIndex.get(phrase);
//                    nbc.phraseProbMap[cIndex][i] = getNewPhraseProbability(cIndex, phraseI,false);
//                    i++;                
//            	}                               
//            }
        }
        MainClass.log("Step 5");

    }
     
    private double getPreProbability(int classIndex) {
        double ret = 0;
        int NC = spamNormalCount[classIndex];
        int N = 0;
        for(int n : spamNormalCount){
            N += n;
        }
        ret = 1.0 * NC / N;

        return ret;
    }
  
    private double getclassRuleProbability(int classIndex, int ruleIndex, boolean contain){
        return getProbability(classIndex, ruleIndex, mRuleCountMap, contain);
    }

    private double getClassConditionlProbability(int classIndex, int wordIndex, boolean contain){
        return getProbability(classIndex, wordIndex, classKeyMap, contain);
    }

    private double getProbability(int classIndex, int termIndex, int[][] map, boolean contain){
        int N = spamNormalCount[classIndex];
        int V = getPropertyCount();
        int NCX = map[classIndex][termIndex];

        double ret = ((double)NCX) / (N);// + V); //laplace smoothing. 拉普拉斯平滑处理

        double a = 0.00001;
        ret = (1.0*a + NCX) / (N + V*a);
        
        if(!contain)
        {
        	ret = 1-ret;
        }
                
        return ret;
    }

    private int getPropertyCount(){
        return vocabulary + mRuleManager.getRuleCount() + mNumNewPhrase;// RuleManager.Rules.length;
    }
}