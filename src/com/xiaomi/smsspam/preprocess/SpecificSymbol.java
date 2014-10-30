package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class SpecificSymbol extends RulePrevious {
	
	private int[][] mStatics = new int[Character.MAX_VALUE + 1][Utils.CLASS_COUNT];
    private Map<Character, Integer> symbols = new HashMap<>();
    private List<Character> curSymbols = new ArrayList<>();
    int mTotal;
    int mSpamCount;
    private static final double MIN_IG = 0.004;

    @Override
    public int subClassCount(){
        return symbols.size();
    }

    @Override
    public String getClassName(int i){
        if(i < subClassCount() && i >= 0){
            String name = super.getClassName(getClass());
            name += "_";
            name += symbols.get(i);
            return padding(name);
        }else{
            return null;
        }
    }

    @Override
    public void train(List<Corpus> cpss) {
        for (Corpus cps: cpss) {
            int classId = cps.getIsSpam() ? Utils.SPAM : Utils.NORMAL;
            Set<Character> alphabet = new HashSet<>();
            for (char c : cps.getOriginalBody().toCharArray()) alphabet.add(c);
            for (char c : alphabet) mStatics[c][classId]++;
            mTotal++;
            if (cps.getIsSpam()) {
                mSpamCount++;
            }
        }
        symbols.clear();
        double[] igs = new double[Character.MAX_VALUE + 1];
        double entropySpam = Utils.getEntropy(mTotal, mSpamCount);
        for(int i = 0; i < mStatics.length; ++i){
            if(Word.getGlossary().containsKey("" + (char) i) || Numbers.isRegularNumber((char)i)){
                continue;
            }
            int hasCount = mStatics[i][Utils.SPAM] + mStatics[i][Utils.NORMAL];
            int noCount = mTotal - hasCount;
            double entropy = entropySpam - (1.0 * hasCount / mTotal) * Utils.getEntropy(hasCount, mStatics[i][Utils.SPAM])
                    - (1.0 * noCount / mTotal) * Utils.getEntropy(noCount, mSpamCount - mStatics[i][Utils.SPAM]);
            igs[i] = entropy;
        }
        List<Double> maxIg = new ArrayList<Double>();
        for(int i = 1; i < igs.length; ++i){
            if(Utils.goodInfo(igs[i], mStatics[i][Utils.SPAM], mStatics[i][Utils.NORMAL])){
                symbols.put((char)i, symbols.size());
                maxIg.add(igs[i]);
            }
        }
        writeCurRules(modelOut, symbols.keySet());
    }

    @Override
    public void reset() {
        curSymbols.clear();
    }

    @Override
    public void updRemainingBody(Corpus cps) {

    }

    @Override
    public void process(Corpus cps) {
        for (String token: cps.getRemainingBody()) {
            for (char c : token.toCharArray()) {
                if (!symbols.containsKey(c)) continue;
                curSymbols.add(c);
            }
        }
        writeCurRules(extractedRulesOut, curSymbols);
        for (char c: curSymbols) {
            cps.getX()[this.getStartIndex() + symbols.get(c)] = 1;
        }

    }

    @Override
	public String getName() {
		return "SpecificSymbol";
	}

	@Override
	public void readDef(DataInputStream dataIn) throws IOException {
		symbols.clear();
		int s = dataIn.readInt();
		for(int i = 0; i < s; i++)
		{
			char c = dataIn.readChar();
			symbols.put(c, i);
		}
		
	}

	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {
		dataOut.writeInt(symbols.size());
		for(int i=0; i < symbols.size(); i++)
		{
			dataOut.writeChar(symbols.get(i));
		}
		
	}
}
