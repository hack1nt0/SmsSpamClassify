package com.xiaomi.smsspam.preprocess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.Tokenizer;
import com.xiaomi.smsspam.Utils.Utils;

public class SpecificSymbol extends RuleUpper {
	
	private int[][] mStatics = new int[Character.MAX_VALUE + 1][Utils.CLASS_COUNT];
    private List<Character> mSymbols = new ArrayList<Character>();
    int mTotal;
    int mSpamCount;

    private static final double MIN_IG = 0.004;
    @Override
    protected int subClassCount(){
        return mSymbols.size();
    }

    @Override
    public String getClassName(int i){
        if(i < subClassCount() && i >= 0){
            String name = super.getClassName(getClass());
            name += "_";
            name += mSymbols.get(i);
            return padding(name);
        }else{
            return null;
        }
    }

    @Override
    public boolean doFitting(Corpus cps, int[] vals, int start) {
        boolean flag = false;
        String body = cps.getOriginBody();
        for(int i = 0; i < mSymbols.size(); ++i){
            if(body.indexOf(mSymbols.get(i)) != -1){
                vals[start + i]++;
                flag = true;
            }
        }
        return flag;
    }

    @Override
    public void firstStepDone(){
    	if(mSymbols.isEmpty())
    	{
        double[] igs = new double[Character.MAX_VALUE + 1];
        double entropySpam = Utils.getEntropy(mTotal, mSpamCount);
        Tokenizer seg = RuleManager.getSeg();
        for(int i = 0; i < mStatics.length; ++i){
            if(seg.inDict("" + (char)i) || Numbers.isRegularNumber((char)i)){
                continue;
            }
            int hasCount = mStatics[i][Utils.SPAM] + mStatics[i][Utils.NORMAL];
            int noCount = mTotal - hasCount;
            double entropy = entropySpam - (1.0 * hasCount / mTotal) * Utils.getEntropy(hasCount, mStatics[i][Utils.SPAM])
                    - (1.0 * noCount / mTotal) * Utils.getEntropy(noCount, mSpamCount - mStatics[i][Utils.SPAM]);
            igs[i] = entropy;
            
        }

        mSymbols = new ArrayList<Character>();
        List<Double> maxIg = new ArrayList<Double>();
        for(int i = 1; i < igs.length; ++i){
            if(Utils.goodInfo(igs[i], mStatics[i][Utils.SPAM], mStatics[i][Utils.NORMAL])){
                mSymbols.add((char)i);
                maxIg.add(igs[i]);
            }
        }
    	}
//        while(true){
//            char maxIndex = 0;
//            double maxInfoG = igs[0];
//            for(int i = 1; i < igs.length; ++i){
//                if(maxInfoG < igs[i]){
//                    maxIndex = (char)i;
//                    maxInfoG = igs[i];
//                }
//            }
//
////            if(maxInfoG > MIN_IG){
//            if(Utils.goodInfo(maxInfoG, mStatics[maxIndex][Utils.SPAM], mStatics[maxIndex][Utils.NORMAL])){
//                mSymbols.add(maxIndex);
//                maxIg.add(maxInfoG);
//                igs[maxIndex] = 0.0;
//            }else{
//                break;
//            }
//        }
        
//        for(int i = 0; i < maxIg.size(); ++i){
//            System.out.println("Symbol:" + mSymbols.get(i) + "\tig:" + maxIg.get(i) + " \t" +
//                        mStatics[mSymbols.get(i)][Utils.SPAM] + ":" + mStatics[mSymbols.get(i)][Utils.NORMAL]);
//        }
    }

    @Override
    protected void doFirstStep(Corpus cps){
        String str = cps.getOriginBody();
        List<Character> lst = new ArrayList<Character>();
        int classId = cps.getIsSpam() ? Utils.SPAM : Utils.NORMAL;
        for(int i = 0; i < str.length(); ++i){
            boolean dup = false;
            for(int j = 0; j < lst.size(); ++j){
                if(str.charAt(i) == lst.get(j)){
                    dup = true;
                    break;
                }
            }
            if(!dup){
                lst.add(str.charAt(i));
                mStatics[str.charAt(i)][classId]++;
            }
        }

        mTotal++;
        if(cps.getIsSpam()){
            mSpamCount++;
        }
    }
    
    @Override
	public String getName() {
		// TODO Auto-generated method stub
		return "SpecificSymbol";
	}

	@Override
	public void readDef(DataInputStream dataIn) throws IOException {
		// TODO Auto-generated method stub
		mSymbols = new ArrayList<Character>();
		int s = dataIn.readInt();
		for(int i = 0; i < s; i++)
		{
			char c = dataIn.readChar();
			mSymbols.add(c);
		}
		
	}

	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {
		// TODO Auto-generated method stub
		dataOut.writeInt(mSymbols.size());
		for(int i=0; i < mSymbols.size(); i++)
		{
			dataOut.writeChar(mSymbols.get(i));
		}
		
	}
}
