package com.xiaomi.smsspam;

import java.util.List;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.Utils;
import com.xiaomi.smsspam.preprocess.RuleManager;
import com.xiaomi.smsspam.preprocess.RulePrevious;
import com.xiaomi.smsspam.preprocess.RuleUpper;

public class NaiveBayesClassifier {
	
	List<String> termDict = new ArrayList<String>();//词典
	RuleManager rmgr = new RuleManager();
	
	double termProbMap[][];//词两类概率
	double ruleProbMap[][];	//规则两类概率
	double classPreProbMap[];//两类概率
	
	double termProbLogMap[][][];
	double ruleProbLogMap[][][];
	double classPreProbLogMap[];
	
	public static final int SPAM = Utils.SPAM;
    public static final int NORMAL = Utils.NORMAL;
    public static final int CLASS_COUNT = Utils.CLASS_COUNT;
	
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
		boolean termArray[] = new boolean[termDict.size()];//词特征

		rmgr.process(cps);		
		List<String> terms = cps.getSegments();//分词结果
		
		int[] rules = cps.getRules();//规则特征
				
		int result = 0;
		
		//termArray数组默认为false，将分词得到的结果每个词对应的termArray的位置置为true
		for(String term:terms)
		{
			int i = binarySearchTermDict(termDict,term);
			if(i >= 0)
			{
				termArray[i] = true;
			}
		}
		
		//计算短信分别属于两类的概率
		LongFloat[] probs = new LongFloat[CLASS_COUNT];
		
		for(int cIndex = 0; cIndex < CLASS_COUNT; ++cIndex)
		{
			probs[cIndex] = new LongFloat(classPreProbMap[cIndex]);
			double multiplier;
			for(int i = 0; i < termArray.length; ++i)
			{
				multiplier = termProbMap[cIndex][i];

				if(termArray[i])
				{
					multiplier = 1.0-multiplier;
				}
				probs[cIndex].multiply(multiplier);
			}
			
			for(int i = 0; i < rmgr.getRuleCount(); ++i)
			{
				multiplier = ruleProbMap[cIndex][i];
				if((rules[i] > 0))
				{
					multiplier = 1.0-multiplier;
				}
				probs[cIndex].multiply(multiplier);
			}										
		}
		result = probs[SPAM].div(probs[NORMAL]) > Options.SPAM_RATIO_THRESHOLD ? SPAM : NORMAL;
        return result == SPAM;		
	}
	
    //对Copus进行分类，垃圾邮件返回true，否则返回false
	public boolean classifyLog(Corpus cps)
	{
		boolean termArray[] = new boolean[termDict.size()];//词特征

		rmgr.process(cps);		
		List<String> terms = cps.getSegments();//分词结果
		
		int[] rules = cps.getRules();//规则特征
				
		int result = 0;
		
		//termArray数组默认为false，将分词得到的结果每个词对应的termArray的位置置为true
		for(String term:terms)
		{
			int i = binarySearchTermDict(termDict,term);
			if(i >= 0)
			{
				termArray[i] = true;
			}
		}
		
		//计算短信分别属于两类的概率
		double[] probs = new double[CLASS_COUNT];
		
		for(int cIndex = 0; cIndex < CLASS_COUNT; ++cIndex)
		{
			probs[cIndex] = classPreProbLogMap[cIndex];
			for(int i = 0; i < termArray.length; ++i)
			{
				int contain;
				if(termArray[i]) contain = 1;
				else contain = 0;
				probs[cIndex] += termProbLogMap[cIndex][i][contain];
			}
			
			for(int i = 0; i < rmgr.getRuleCount(); ++i)
			{
				int contain;
				if(rules[i] > 0) contain = 1;
				else contain = 0;
				probs[cIndex] += ruleProbLogMap[cIndex][i][contain];
			}										
		}
		result = probs[SPAM]-probs[NORMAL] > Options.SPAM_RATIO_THRESHOLD_LOG ? SPAM : NORMAL;
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
	
	//读取模型文件
	public void readModel() throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException
	{
		RuleManager.mNewSeg.readDict();
		
		FileInputStream fileIn = new FileInputStream(Options.MODEL_FILE_PATH);
	    DataInputStream dataIn = new DataInputStream(fileIn);
	    
        classPreProbMap = new double[CLASS_COUNT];
        classPreProbMap[0] = dataIn.readDouble();
        classPreProbMap[1] = dataIn.readDouble();

        int termNumber = dataIn.readInt();
        termProbMap = new double[CLASS_COUNT][termNumber];        
        for(int i = 0; i < termNumber; i++)
        {
        	String str = "";
        	char c;
        	while((c = dataIn.readChar())!='\n')
        	{
        		str += c;
        	}
        	termDict.add(str);
        }
        for(int i = 0; i < termNumber; i++)
        {
        	termProbMap[0][i] = dataIn.readDouble();
        }
        for(int i = 0; i < termNumber; i++)
        {
        	termProbMap[1][i] = dataIn.readDouble();
        }
        
        int ruleNumber = dataIn.readInt();
        ruleProbMap = new double[CLASS_COUNT][ruleNumber];
        
        int rulePreviousNumber = dataIn.readInt();
        RulePrevious[] rp = new RulePrevious[rulePreviousNumber];
        for(int i = 0; i < rulePreviousNumber; i++)
        {
        	String str = "";
        	char c;
        	while((c = dataIn.readChar())!='\n')
        	{
        		str += c;
        	}
        	rp[i] = (RulePrevious) Class.forName(Options.PREPROCESS_PACKAGE+"."+str).newInstance();
        	rp[i].readDef(dataIn);
        }
        int ruleUpperNumber = dataIn.readInt();
        RuleUpper[] ru = new RuleUpper[ruleUpperNumber];
        for(int i = 0; i < ruleUpperNumber; i++)
        {
        	String str = "";
        	char c;
        	while((c = dataIn.readChar())!='\n')
        	{
        		str += c;
        	}
        	ru[i] = (RuleUpper) Class.forName(Options.PREPROCESS_PACKAGE+"."+str).newInstance();
        	ru[i].readDef(dataIn);
        }
        rmgr = new RuleManager(rp,ru);
		rmgr.initPreRules();
		rmgr.initUpperRules();

        for(int i = 0; i < ruleNumber; i++)
        {
        	ruleProbMap[0][i] = dataIn.readDouble();
        }
        for(int i = 0; i < ruleNumber; i++)
        {
        	ruleProbMap[1][i] = dataIn.readDouble();
        }
        
        fileIn.close();
        dataIn.close();
	}	
	
}