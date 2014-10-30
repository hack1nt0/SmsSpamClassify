package com.xiaomi.smsspam.preprocess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

import com.xiaomi.smsspam.*;
import com.xiaomi.smsspam.Utils.*;


public class NewPhrase extends RulePrevious {


    private Map<String, PairCount>[] mFilteredPhrases; //the new-different-length phrase and its statistical info
    private static final int[] PHRASE_LENGTH = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
    private SimpleDict mDict;

    private List<String> curNewPhrases;

    public NewPhrase() {
        /*
        this.mFilteredPhrases = new HashMap<String, PairCount>[0];
        this.mDict = mDict;*/
        this.curNewPhrases = new ArrayList<>();
    }

    public int getPhraseCount(){
        if(mFilteredPhrases == null){
            return 0;
        }
        int count = 0;
        for(Map<String, PairCount> map : mFilteredPhrases){
            count += map.size();
        }
        return count;
    }

    public Map<String, Integer> getPhraseMap(){
        Map<String, Integer> res = new HashMap<String, Integer>();
        int index = 0;
        for(Map<String, PairCount> map : mFilteredPhrases){
            for (Map.Entry<String, PairCount> entry : map.entrySet()) {
                res.put(entry.getKey(), index++);
            }
        }
        return res;
    }

    public void checkSubWord(List<Integer> ids, List<String> phs){
        int removedCount = 0;
        if(!Options.TEST_ALG){
//            System.modelOut.println("Totally Count:" + getPhraseCount());
        }
        for(Map<String, PairCount> map : mFilteredPhrases){
            Set<String> toRemove = new HashSet<String>();
            Iterator<Map.Entry<String, PairCount>> it = map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, PairCount> entry = it.next();
                String key = entry.getKey();
                if(shouldRemove(key, ids, phs)){
                    toRemove.add(key);
                }
            }
            for(String key : toRemove){
                map.remove(key);
                removedCount++;
            }
        }
/*        if(!Options.TEST_ALG){
            System.modelOut.println("removed:" + removedCount);
            int leftCount = 0;
            for(Map<String, PairCount> map : mFilteredPhrases){
                Iterator<Map.Entry<String, PairCount>> it = map.entrySet().iterator();
                while(it.hasNext()){
                    System.modelOut.println("Left phrase::" + it.next().getKey());
                    leftCount++;
                }
            }
            System.modelOut.println("leftCount:" + leftCount);
        }*/
    }

    private boolean shouldRemove(String p, List<Integer> ids, List<String> phs){
        for(int i : ids){
            if(phs.get(i).indexOf(p) != -1){
                return true;
            }
        }
        return false;
    }

    public boolean containPhrase(String sms, String phrase){
        return sms.indexOf(phrase) != -1;
    }

    public SimpleDict getDict(){
        if(null == mDict){
            mDict = new SimpleDict();
            mDict.load(mFilteredPhrases);
        }
        return mDict;
    }

    public Map<String, PairCount>[] getFilteredPhrases(){
        return mFilteredPhrases;
    }

    private static boolean isChineseCharacter(char c) {
        return 19968 <= c && c <= 171941;
    }

    public static void main(String[] args) {
        String s = "代办大额信用卡及贷款，速度快效率高，代还信用卡及贷款，个人抵押贷款最快可当天放款。电话010--57277666，";
        for (char c: s.toCharArray()) {
            if (!isChineseCharacter(c)) continue;
            System.out.print(c);
        }
        System.out.println();
    }

    // Remove child
    private void filterPhrase(Map<String, PairCount>[] filteredPhrases){
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
//                                System.modelOut.println(phrs + ":" + vals[0] + ",\t" + vals[1] + "\t\t"
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
//                System.modelOut.println("--------------------------------------" + PHRASE_LENGTH[i]);

                while(it.hasNext()){
                    Map.Entry<String, PairCount> entry = it.next();
                    String key = entry.getKey();
                    PairCount pc = entry.getValue();
//                    System.modelOut.println(key + ":" + pc.vals[0] + ",\t" + pc.vals[1]);
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


    @Override
    public void train(List<Corpus> cpss) {
        Map<String, int[]>[] mPhrase = new HashMap[PHRASE_LENGTH.length];
        for(int i = 0; i < PHRASE_LENGTH.length; ++i){
            mPhrase[i] = new HashMap<String, int[]>();
        }

        Tokenizer tokenizer = Word.getSeg();
        List<String> phrases = new LinkedList<String>();

        int totalCount = cpss.size();
        int spamCount = 0;

        for(int i = 0; i < cpss.size(); ++i){
            Corpus cps = cpss.get(i);
            if(cps.getIsSpam()){
                spamCount++;
            }
            String body = cps.getOriginalBody();

            //cps.setRefinedBody(body);
            boolean[] flags = new boolean[body.length()];
            for(int j = 0; j < body.length(); ++j){
                //  flags[j] = segment.inDict("" + body.charAt(j));
                flags[j] = isChineseCharacter(body.charAt(j));
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
                        //System.modelOut.println(body + ",\t" + "j:" + j + ",\tPHRASE_LENGTH[ii]:"  + PHRASE_LENGTH[ii]);
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
                        //System.modelOut.println("Add new phrase: " + phs);
                    }
                    mPhrase[ii].get(phs)[cps.getIsSpam() ? Options.SPAM : Options.NORMAL]++;
                }
            }
        }

        double entropySpam = Utils.getEntropy(totalCount, spamCount);

        mFilteredPhrases = new HashMap[PHRASE_LENGTH.length];
        for(int i = 0; i < PHRASE_LENGTH.length; ++i){
            mFilteredPhrases[i] = new HashMap<String, PairCount>();
        }

        for(int ii = 0; ii < PHRASE_LENGTH.length; ++ii){
            List<PairCount> sortPair = new ArrayList<PairCount>();
            for (Map.Entry<String, int[]> entry: mPhrase[ii].entrySet()) {
                String ph = entry.getKey();
                int[] values = entry.getValue();

                if(values[0] + values[1] > (totalCount / 1000)){
                    int hasCount = values[0] + values[1];
                    int noCount = totalCount - hasCount;
                    double ig = entropySpam - (1.0 * hasCount / totalCount) * Utils.getEntropy(hasCount, values[0])
                            - (1.0 * noCount / totalCount) * Utils.getEntropy(noCount, spamCount - values[0]);

                    if(ig > Options.NEW_PHRASE_IG_THRESH_VALUE){
                        PairCount pc = new PairCount(ph, values);
                        pc.ig = ig;
                        sortPair.add(pc);
                    }
                }
            }
            Collections.sort(sortPair, new Comparator<PairCount>(){
                public int compare(PairCount arg0, PairCount arg1) {
                    return Double.compare(arg1.ig, arg0.ig);
                }
            });

            for(PairCount pc : sortPair){
                if(Options.NEW_PHRASE_IG_THRESH_VALUE > pc.ig ){
                    break;
                }
                mFilteredPhrases[ii].put(pc.phs, pc);
            }
        }
        filterPhrase(mFilteredPhrases);
    }

    @Override
    public void reset() {
        curNewPhrases.clear();
    }

    @Override
    public void updRemainingBody(Corpus cps) {

    }

    @Override
    public void process(Corpus cps) {
        //TODO
        //cps.getTokens().addAll(curNewPhrases);
    }

    @Override
    public int subClassCount() {
        return 0;
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {

    }
}
