package com.xiaomi.smsspam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xiaomi.smsspam.preprocess.RuleManager;


public class NewPhraseExplorer {

	private Map<String, PairCount>[] mFilteredPhrases;
    private static final int[] PHRASE_LENGTH = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    private SimpleDict mDict;

    public int getPhraseCount(){
        int count = 0;
        if(mFilteredPhrases == null){
            return count;
        }
        for(Map<String, PairCount> map : mFilteredPhrases){
            count += map.size();
        }
        return count;
    }

    public Map<String, Integer> getPhraseMap(){
        Map<String, Integer> pi = new HashMap<String, Integer>();
        int index = 0;
        for(Map<String, PairCount> map : mFilteredPhrases){
            Iterator<Map.Entry<String, PairCount>> it = map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, PairCount> entry = it.next();
                pi.put(entry.getKey(), index++);
            }
        }
        return pi;
    }

    public void checkSubWord(List<Integer> ids, List<String> phs){
        int removedCount = 0;
        if(!Options.TEST_ALG){
            System.out.println("Totally Count:" + getPhraseCount());
        }
        for(Map<String, PairCount> map : mFilteredPhrases){
            Set<String> toRemove = new HashSet<String>();
            Iterator<Map.Entry<String, PairCount>> it = map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, PairCount> entry = it.next();
                String key = entry.getKey();
                if(contain(key, ids, phs)){
                    toRemove.add(key);
                }
            }
            for(String key : toRemove){
                map.remove(key);
                removedCount++;
            }
        }
        if(!Options.TEST_ALG){
            System.out.println("removed:" + removedCount);
            int leftCount = 0;
            for(Map<String, PairCount> map : mFilteredPhrases){
                Iterator<Map.Entry<String, PairCount>> it = map.entrySet().iterator();
                while(it.hasNext()){
                    System.out.println("Left phrase::" + it.next().getKey());
                    leftCount++;
                }
            }
            System.out.println("leftCount:" + leftCount);
        }
    }

    private boolean contain(String p, List<Integer> ids, List<String> phs){
        for(int i : ids){
            if(phs.get(i).indexOf(p) != -1){
                if(!Options.TEST_ALG){
                    System.out.println("New phrase included--------------------------------------------------:" + phs.get(i) + "( " + p + ")");
                }
                return true;
            }
//            if(p.indexOf(phs.get(i)) != -1){
//                if(Options.TEST_ALG){
//                    System.out.println("New phrase includ----------------------------------------------------:" + phs.get(i) + "( " + p + ")");
//                }
//                return true;
//            }
        }
        return false;
    }

    public boolean containPhrase(String sms, String phrase){
        String body = preProcess(sms);
        return body.indexOf(phrase) != -1;
    }

    private String preProcess(String str){
        String body = str;
        body = body.replaceAll("\\t", "");
        body = body.replaceAll("\t", "");
        body = body.replaceAll("\\r", "");
        body = body.replaceAll("\r", "");
        body = body.replaceAll("\\n", "");
        body = body.replaceAll("\n", "");
        return body;
    }

    public SimpleDict getDict(){
        if(null == mDict){
            mDict = new SimpleDict();
            mDict.load(mFilteredPhrases);
        }
        return mDict;
    }

    public void doExploring(List<Corpus> orignCorpus){
        Map<String, int[]>[] mPhrase = new HashMap[PHRASE_LENGTH.length];
        for(int i = 0; i < PHRASE_LENGTH.length; ++i){
            mPhrase[i] = new HashMap<String, int[]>();
        }

        Tokenizer tokenizer = RuleManager.getSeg();
        List<String> phrases = new LinkedList<String>();

        int totalCount = orignCorpus.size();
        int spamCount = 0;

        for(int i = 0; i < orignCorpus.size(); ++i){
            Corpus cps = orignCorpus.get(i);
            if(cps.getIsSpam()){
                spamCount++;
            }
            String body = cps.getOriginBody();
            body = preProcess(body);

            cps.setRefinedBody(body);
            boolean[] flags = new boolean[body.length()];
            for(int j = 0; j < body.length(); ++j){
                flags[j] = tokenizer.inDict("" + body.charAt(j));//TODO
            }

            for(int ii = 0; ii < PHRASE_LENGTH.length; ++ii){
                phrases.clear();
                int serialCount = 0;
                for(int j = 0; j < body.length(); ++j){
                    if(flags[j]){
                        serialCount++;
                    }else{
                        serialCount = 0;
                    }
                    if(serialCount == PHRASE_LENGTH[ii]){
                        //System.out.println(body + ",\t" + "j:" + j + ",\tPHRASE_LENGTH[ii]:"  + PHRASE_LENGTH[ii]);
                        String phrase = body.substring(j - PHRASE_LENGTH[ii] + 1, j + 1);
                        if(!phrases.contains(phrase) && !tokenizer.inDict(phrase)){
                            phrases.add(phrase);
                        }
                        serialCount--;
                    }
                }
                for(String phs : phrases){
                    if(!mPhrase[ii].containsKey(phs)){
                        mPhrase[ii].put(phs, new int[]{0, 0});
                        //System.out.println("Add new phrase: " + phs);
                    }
                    mPhrase[ii].get(phs)[cps.getIsSpam() ? NaiveBayesManager.SPAM : NaiveBayesManager.NORMAL]++;
                }
            }
        }

        double entropySpam = Utils.getEntropy(totalCount, spamCount);

        mFilteredPhrases = new HashMap[PHRASE_LENGTH.length];
        for(int i = 0; i < PHRASE_LENGTH.length; ++i){
            mFilteredPhrases[i] = new HashMap<String, PairCount>();
        }

        for(int ii = 0; ii < PHRASE_LENGTH.length; ++ii){
            Iterator<Map.Entry<String, int[]>> it = mPhrase[ii].entrySet().iterator();
            List<PairCount> sortPair = new ArrayList<PairCount>();
            while(it.hasNext()){
                Map.Entry<String, int[]> entry = it.next();
                String ph = entry.getKey();
                int[] values = entry.getValue();

                if(values[0] + values[1] > (totalCount / 1000)){
                    int hasCount = values[0] + values[1];
                    int noCount = totalCount - hasCount;
                    double infog = entropySpam - (1.0 * hasCount / totalCount) * Utils.getEntropy(hasCount, values[0])
                                - (1.0 * noCount / totalCount) * Utils.getEntropy(noCount, spamCount - values[0]);

                    if(infog > Options.NEW_PHRASE_IG_THRESH_VALUE){
                        PairCount pc = new PairCount(ph, values);
                        pc.ig = infog;
                        sortPair.add(pc);
                    }
                }
            }
            Collections.sort(sortPair, new Comparator<PairCount>(){
                public int compare(PairCount arg0, PairCount arg1) {
                    return Double.compare(arg1.ig, arg0.ig);
                }
            });

//            int count = 0;
//            System.out.println("---------------------------   " + PHRASE_LENGTH[ii] + "   -----------------------------");
            for(PairCount pc : sortPair){
                if(Options.NEW_PHRASE_IG_THRESH_VALUE > pc.ig ){
                    break;
                }
//                System.out.println(pc.phs + ":" + pc.ig + ",\t" + pc.vals[0] + ",\t" + pc.vals[1]);
                mFilteredPhrases[ii].put(pc.phs, pc);
//                count++;
            }
//            System.out.println("---------------------------   " + PHRASE_LENGTH[ii] + "   -----------------------------" + count);
        }
        filterPhrase(mFilteredPhrases);

        for(int i = 0; i < mPhrase.length; ++i){
            mPhrase[i].clear();
            mPhrase[i] = null;
        }
        mPhrase = null;

        //new Tables(orignCorpus, mFilteredPhrases);
    }

    private void filterPhrase(Map<String, PairCount>[] filteredPhrases){
        // Remove child if has
        for(int i = filteredPhrases.length - 1; i >= 0; --i){
            Map<String, PairCount> map = filteredPhrases[i];
            Object[] phrases = map.keySet().toArray();
            for(int j = 0; j < phrases.length; ++j){
                String phrs = (String)phrases[j];
                int[] vals = map.get(phrs).vals;
                for(int k = i - 1; k >= 0; --k){
                    Map<String, PairCount> kmap = filteredPhrases[k];
                    Set<String> toRemove = new HashSet<String>();
                    
                    Iterator<Map.Entry<String, PairCount>> it = kmap.entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry<String, PairCount> entry = it.next();
                        String key = entry.getKey();
                        PairCount pc = entry.getValue();
                        if(phrs.indexOf(key) != -1){
                            if(canRemoveChild(vals, pc.vals)){
//                                System.out.println(phrs + ":" + vals[0] + ",\t" + vals[1] + "\t\t"
//                                            + key + ":" + pc.vals[0] + ",\t" + pc.vals[1] + "\t Removed");
                                toRemove.add(key);
                                pc.dup = true;
                            }
                        }
                    }
                    for(String key : toRemove){
                        kmap.remove(key);
                    }
                }
            }
        }


        if(!Options.TEST_ALG){
            int totalCount = 0;
            for(int i = filteredPhrases.length - 1; i >= 0; --i){
                Map<String, PairCount> map = filteredPhrases[i];
                Iterator<Map.Entry<String, PairCount>> it = map.entrySet().iterator();
                System.out.println("--------------------------------------" + PHRASE_LENGTH[i]);

                while(it.hasNext()){
                    Map.Entry<String, PairCount> entry = it.next();
                    String key = entry.getKey();
                    PairCount pc = entry.getValue();
                    System.out.println(key + ":" + pc.vals[0] + ",\t" + pc.vals[1]);
                    totalCount++;
                }
            }
            System.out.println("Total New Phrases Count:" + totalCount);
        }
    }

    private boolean canRemoveChild(int[] parent, int[] child){
        int ps = parent[0] + parent[1];
        int cs = child[0] + child[1];
        if((1.0 * ps / cs) > 0.6){
            return true;
        }
        return false;
    }

//    public void testSegs(){
//        mFilteredPhrases = new HashMap[PHRASE_LENGTH.length];
//        for(int i = 0; i < PHRASE_LENGTH.length; ++i){
//            mFilteredPhrases[i] = new HashMap<String, PairCount>();
//        }
//        PairCount pc = new PairCount("江市1", new int[]{1,2});
//        pc.ig = 0.02;
//        mFilteredPhrases[pc.phs.length()].put(pc.phs, pc);
//
//        pc = new PairCount("套全1", new int[]{1,2});
//        pc.ig = 0.03;
//        mFilteredPhrases[pc.phs.length()].put(pc.phs, pc);
//
//
//        pc = new PairCount("名牌小车1", new int[]{1,2});
//        pc.ig = 0.02;
//        mFilteredPhrases[pc.phs.length()].put(pc.phs, pc);
//
//
//        pc = new PairCount("牌小1", new int[]{1,2});
//        pc.ig = 0.03;
//        mFilteredPhrases[pc.phs.length()].put(pc.phs, pc);
//
//        String test = "政府主办，全城轰动！晋江市第二届人居节200套全城最优惠特价房、名牌小车和众多奖品，等你来拿！报名热线";
//
//        SimpleDict sd = new SimpleDict();
//        sd.load(mFilteredPhrases);
//        List<String>[] ret = sd.cut(test);
//        for(List<String> ls : ret){
//            System.out.println("--------------------------NORMAL/NEW------------------------");
//            for(String s : ls){
//                System.out.println(s);
//            }
//        }
//    }
//
//    public static void main(String[] args) {
//        NewPhraseExplorer ep = new NewPhraseExplorer();
//        ep.testSegs();
//    }
}
