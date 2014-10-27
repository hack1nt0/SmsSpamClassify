package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Assume the length of spam of SMS is longer than normal
public class SmsLength extends RulePrevious {
	
	List<int[]> mStatics = new ArrayList<int[]>();
    int mTotal;
    int mSpamCount;
    int mDivideLength;
    int curLength;

    private static final int HIT = 0;
    private static final int MISS = 1;

    @Override
    public int subClassCount() {
        return 1;
    }

    @Override
    public void train(List<Corpus> cpss) {
        mStatics.clear();//TODO
        for (Corpus cps: cpss) {
            int len = length(cps);
            int classId = cps.getIsSpam() ? Utils.SPAM : Utils.NORMAL;
            while (mStatics.size() < len + 1) {
                mStatics.add(new int[Utils.CLASS_COUNT]);
            }
            mStatics.get(len)[classId]++;
            mTotal++;
            if (cps.getIsSpam()) {
                mSpamCount++;
            }
        }

        List<Double> igs = new ArrayList<Double>();
        double entropySpam = Utils.getEntropy(mTotal, mSpamCount);

        for(int i = 0; i < mStatics.size(); ++i){
            int[][] counts = new int[Utils.CLASS_COUNT][Utils.CLASS_COUNT];
            for(int j = 0; j < i; ++j){
                counts[Utils.NORMAL][HIT] += mStatics.get(j)[Utils.NORMAL];
                counts[Utils.SPAM][MISS] += mStatics.get(j)[Utils.SPAM];
            }
            for(int j = i; j < mStatics.size(); ++j){
                counts[Utils.SPAM][HIT] += mStatics.get(j)[Utils.SPAM];
                counts[Utils.NORMAL][MISS] += mStatics.get(j)[Utils.NORMAL];
            }
            int lessCount = counts[Utils.NORMAL][HIT] + counts[Utils.SPAM][MISS];
            int moreCount = counts[Utils.SPAM][HIT] + counts[Utils.NORMAL][MISS];
            double entropy = entropySpam - (1.0 * lessCount / mTotal) * Utils.getEntropy(lessCount, counts[Utils.NORMAL][HIT])
                    - (1.0 * moreCount / mTotal) * Utils.getEntropy(moreCount, counts[Utils.SPAM][HIT]);
            igs.add(entropy);
        }
        double maxEntropy = 0.0d;
        for(int i = 0; i < igs.size(); ++i){
            if(igs.get(i) > maxEntropy){
                maxEntropy = igs.get(i);
                mDivideLength = i;
            }
        }


    }

    private int length(Corpus cps) {
        return cps.getOriginBody().length();
    }

    @Override
    public void reset() {

    }

    @Override
    public boolean fit(Corpus cps, int startIndex) {
        if(curLength >= mDivideLength){
            cps.getRulesPreHits()[startIndex]++;
            return true;
        }
        return false;
    }

    @Override
    protected List<String> process(String str) {
        curLength = str.length();
        List<String> res = new ArrayList<>(); res.add(str);
        return res;
    }

    @Override
	public String getName() {
		return "SmsLength";
	}


	@Override
	public void readDef(DataInputStream dataIn) throws IOException {
		mDivideLength = dataIn.readInt();
	}


	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {
		dataOut.writeInt(mDivideLength);
		
	}
}
