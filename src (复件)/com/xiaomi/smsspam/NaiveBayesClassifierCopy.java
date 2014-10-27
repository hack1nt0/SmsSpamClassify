package com.xiaomi.smsspam;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.Utils;
import com.xiaomi.smsspam.preprocess.RuleManager;

import java.io.*;
import java.util.*;

public class NaiveBayesClassifierCopy {
	
	List<String> termDict = new ArrayList<String>();//词典
	RuleManager rmgr = new RuleManager();
	
	double termProbMap[][];//词两类概率
	double ruleProbMap[][];	//规则两类概率
	double classPreProbMap[];//两类概率
	
	public static final int SPAM = Utils.SPAM;
    public static final int NORMAL = Utils.NORMAL;
    public static final int CLASS_COUNT = Utils.CLASS_COUNT;
    private HashMap<String, Integer> termID;
    private int[] termSum;

    //termDict按照字典顺序存储，本函数用于对termDict进行二分查找
    //若key在termDict中，返回key的位置坐标，否则返回-1
    int binarySearchTermDict(List<String> termDict,String key)
    {
    	int imin = 0;
    	int imax = termDict.size()-1;
    	while(imax >= imin)
    	{
    		int imid = imin+(imax-imin)/2;
    		if(termDict.get(imid).equals(key))
    		{
    			return imid;
    		}
    		else if(termDict.get(imid).compareTo(key)<0)
    		{
    			imin = imid+1;
    		}
    		else
    		{
    			imax = imid-1;
    		}    			
    	}
    	return -1;	
    }
    
    //对Copus进行分类，垃圾邮件返回true，否则返回false
	public boolean classify(Corpus cps)
	{
        if (!cps.isProceeded)
            rmgr.process(cps);
		List<String> terms = cps.getSegments();//分词结果
		int[] rules = cps.getRules();//规则特征

		//计算短信分别属于两类的概率
		LongFloat[] probs = new LongFloat[CLASS_COUNT];
		
		for(int cIndex = 0; cIndex < CLASS_COUNT; ++cIndex)
		{
			probs[cIndex] = new LongFloat(classPreProbMap[cIndex]);

			double multiplier;
			for(int i = 0; i < rmgr.getRuleCount(); ++i) {
                multiplier = ruleProbMap[cIndex][i];
                if (rules[i] == 0) {
                    multiplier = 1.0 - multiplier;
                }
                probs[cIndex].multiply(multiplier);
            }

            for (String term: terms) {
                if (termID.containsKey(term))
                    multiplier = termProbMap[cIndex][termID.get(term)];
                else
                    multiplier = 1.0 / (termSum[cIndex] + Options.V);
                probs[cIndex].multiply(multiplier);
            }
		}
		int result = probs[SPAM].div(probs[NORMAL]) > Options.SPAM_RATIO_THRESHOLD ? SPAM : NORMAL;
        return result == SPAM;
	}
	
	//存储模型文件
	public void saveModel() throws IOException
	{
      FileOutputStream fileOut = new FileOutputStream(Options.MODEL_FILE_PATH);
      DataOutputStream dataOut = new DataOutputStream(fileOut);

      int termNumber = termDict.size();
      int ruleNumber = rmgr.getRuleCount();
      dataOut.writeDouble(classPreProbMap[0]);
      dataOut.writeDouble(classPreProbMap[1]);
      dataOut.writeInt(termNumber);        
      for(int i = 0; i < termNumber; i++)
      {
    	  dataOut.writeChars((termDict.get(i)+"\n"));
      }
      for(int i = 0; i < termNumber; i++)
      {
      	dataOut.writeDouble(termProbMap[0][i]);
      }
      for(int i = 0; i < termNumber; i++)
      {
      	dataOut.writeDouble(termProbMap[1][i]);
      }
      
      dataOut.writeInt(ruleNumber);      
      dataOut.writeInt(rmgr.mRulesPrevious.length);
      for(int i = 0; i < rmgr.mRulesPrevious.length; i++)
      {
    	  dataOut.writeChars(rmgr.mRulesPrevious[i].getName()+"\n");
    	  rmgr.mRulesPrevious[i].writeDef(dataOut);
      }
      dataOut.writeInt(rmgr.mRulesUpper.length);
      for(int i = 0; i < rmgr.mRulesUpper.length; i++)
      {
    	  dataOut.writeChars(rmgr.mRulesUpper[i].getName()+"\n");
    	  rmgr.mRulesUpper[i].writeDef(dataOut);
      }      
      for(int i = 0; i < ruleNumber; i++)
      {
      	dataOut.writeDouble(ruleProbMap[0][i]);
      }
      for(int i = 0; i < ruleNumber; i++)
      {
      	dataOut.writeDouble(ruleProbMap[1][i]);
      }     
      fileOut.close();
      dataOut.close();
	}
	

//	//对样本进行特征提取，返回值为1的特征链表，用于稀疏矩阵的存储
	public List<Integer> extractFeature(Corpus cps)
	{
		List<Integer> ret = new ArrayList<Integer>();
		rmgr.process(cps);		
		List<String> terms = cps.getSegments();//分词结果
		
		int[] rules = cps.getRules();//规则特征
				
		
		//termArray数组默认为false，将分词得到的结果每个词对应的termArray的位置置为true
		for(String term:terms)
		{
			int i = binarySearchTermDict(termDict,term);
			if(i >= 0)
			{
				ret.add(i+1);
			}
		}
		for(int i = 0; i < rules.length; i++)
		{
			if(rules[i] > 0)
			{
				ret.add(termDict.size()+i+1);
			}
		}
		return ret;
	}

    public void trainModel(List<Corpus> trainSet) {

        termID = new HashMap<String, Integer>();
        rmgr.process(trainSet);

        for (Corpus cps: trainSet) {
            List<String> terms = cps.getSegments();//分词结果
            for (String term : terms) {
                if (termID.containsKey(term))
                    continue;
                termID.put(term, termID.size());
            }
        }
        termDict = new ArrayList<String>(termID.keySet());

        termProbMap = new double[CLASS_COUNT][termID.size()];
        ruleProbMap = new double[CLASS_COUNT][rmgr.getRuleCount()];
        classPreProbMap = new double[CLASS_COUNT];

        for (Corpus cps: trainSet) {
            List<String> terms = cps.getSegments();//分词结果
            termSum = new int[CLASS_COUNT];
            int classNO = cps.getIsSpam() ? SPAM : NORMAL;

            classPreProbMap[classNO]++;

            for (String term : terms) {
                termProbMap[classNO][termID.get(term)] += 1.0;
                termSum[classNO]++;
            }

            for (int i = 0; i < termID.size(); ++i)
                for (int j = 0; j < Utils.CLASS_COUNT; ++j) {
                    double tmp = (termProbMap[j][i] + 1) / (termSum[j] + Options.V);
                    if (tmp == 0) {
                        System.out.println("(termProbMap[j][i] + 1) / (termSum[j] + Options.V)");
                        System.out.println(termProbMap[j][i] + " " + termSum[j] + " " + Options.V);
                    }
                    termProbMap[j][i] = tmp;
                }

            int[] rules = cps.getRules();
            for (int i = 0; i < rules.length; ++i) {
                ruleProbMap[classNO][i] += rules[i] > 0 ? 1 : 0;
            }
        }

        for (int i = 0; i < CLASS_COUNT; ++i)
            for (int j = 0; j < ruleProbMap[0].length; ++j) {
                double tmp = (ruleProbMap[i][j] + 1) / (classPreProbMap[i] + 2);
                if (tmp > 1) {
                    System.out.println("(ruleProbMap[i][j] + 1) / (classPreProbMap[i] + 2)");
                    System.out.println(ruleProbMap[i][j] + " " + classPreProbMap[i]);
                }
                ruleProbMap[i][j] = tmp;
            }

        //laplace smoothing is not necessary here
        for (int i = 0; i < CLASS_COUNT; ++i)
            classPreProbMap[i] /= trainSet.size();

    }

    public static void main(String[] args) {
        Corpus cps = new Corpus();
        cps.setOriginBody("鑫磊玻璃那处房产抵押给我们了，别开行批了你们没东西押了");
        NaiveBayesClassifierCopy nbc = new NaiveBayesClassifierCopy();
        nbc.rmgr.process(cps);
        System.out.println(cps.getSegments());
        System.out.println(nbc.classify(cps));
    }

}