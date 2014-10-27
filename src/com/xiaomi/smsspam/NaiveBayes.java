package com.xiaomi.smsspam;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.LongFloat;
import com.xiaomi.smsspam.Utils.Utils;
import com.xiaomi.smsspam.preprocess.RuleManager;
import com.xiaomi.smsspam.preprocess.Word;

import java.io.*;
import java.util.*;

public class NaiveBayes extends Classifier{
	
	double termProbMap[][];//词两类概率
	double ruleProbMap[][];	//规则两类概率
	double classPreProbMap[];//两类概率
	
	public static final int SPAM = Utils.SPAM;
    public static final int NORMAL = Utils.NORMAL;
    public static final int CLASS_COUNT = Utils.CLASS_COUNT;
    private Map<String, Integer> termID;
    private int[] termSum;

    //对Copus进行分类，垃圾邮件返回true，否则返回false
	public boolean classify(Corpus cps)
	{
		List<String> terms = cps.getSegments();//分词结果
		int[] rules = cps.getRulesPreHits();//规则特征

		//计算短信分别属于两类的概率
		LongFloat[] probs = new LongFloat[CLASS_COUNT];
		
		for(int classNO = 0; classNO < CLASS_COUNT; ++classNO)
		{
			probs[classNO] = new LongFloat(classPreProbMap[classNO]);

			double multiplier;
			for(int i = 0; i < cps.getRulesPreHits().length; ++i) {
                multiplier = ruleProbMap[classNO][i];
                if (rules[i] == 0) {
                    multiplier = 1.0 - multiplier;
                }
                probs[classNO].multiply(multiplier);
            }

            for (String term: terms) {
                int tid = termID.containsKey(term) ? termID.get(term) : termID.size();
                probs[classNO].multiply(termProbMap[classNO][tid]);
            }
		}
		int result = probs[SPAM].div(probs[NORMAL]) > Options.SPAM_RATIO_THRESHOLD ? SPAM : NORMAL;
        return result == SPAM;
	}

    @Override
    public void saveModel(String filePath) {
        /*
        try {

            FileOutputStream fileOut = new FileOutputStream(filePath);
            DataOutputStream dataOut = new DataOutputStream(fileOut);

            int termNumber = termID.size();
            int ruleNumber = ruleProbMap[0].length;
            dataOut.writeDouble(classPreProbMap[0]);
            dataOut.writeDouble(classPreProbMap[1]);
            dataOut.writeInt(termNumber);
            for (String term: termID.keySet()) {
                dataOut.writeChars((term + "\n"));
            }
            for (int i = 0; i < termNumber; i++) {
                dataOut.writeDouble(termProbMap[0][i]);
            }
            for (int i = 0; i < termNumber; i++) {
                dataOut.writeDouble(termProbMap[1][i]);
            }

            dataOut.writeInt(ruleNumber);
            dataOut.writeInt(ruleManager.getRuleObjs().length);
            for (int i = 0; i < ruleManager.getRuleObjs().length; i++) {
                dataOut.writeChars(ruleManager.getRuleObjs()[i].getName() + "\n");
                ruleManager.getRuleObjs()[i].writeDef(dataOut);
            }
            for (int i = 0; i < ruleNumber; i++) {
                dataOut.writeDouble(ruleProbMap[0][i]);
            }
            for (int i = 0; i < ruleNumber; i++) {
                dataOut.writeDouble(ruleProbMap[1][i]);
            }
            fileOut.close();
            dataOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
	}

    @Override
    public void readModel(String filePath) {
        //TODO
    }

    public void train(List<Corpus> trainSet) {

        termID = Word.getGlossary();
        termProbMap = new double[CLASS_COUNT][termID.size() + 1];
        ruleProbMap = new double[CLASS_COUNT][RuleManager.getRuleCnt()];
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

            int[] rules = cps.getRulesPreHits();
            for (int i = 0; i < rules.length; ++i) {
                ruleProbMap[classNO][i] += rules[i] > 0 ? 1 : 0;
            }
        }

        //termProbMap
        for (int i = 0; i < Utils.CLASS_COUNT; ++i) {
            for (int j = 0; j <= termID.size(); ++j) {
                double tmp = (termProbMap[i][j] + 1) / (termSum[i] + termID.size() + 1);
                assert tmp == 0;
                /*
                if (tmp == 0) {
                    System.out.println("(termProbMap[j][i] + 1) / (termSum[j] + Options.V)");
                    System.out.println(termProbMap[j][i] + " " + termSum[j] + " " + (termDict.size() + 1));
                }*/
                termProbMap[i][j] = tmp;
            }
        }

        //ruleProbMap
        for (int i = 0; i < CLASS_COUNT; ++i)
            for (int j = 0; j < ruleProbMap[0].length; ++j) {
                double tmp = (ruleProbMap[i][j] + 1) / (classPreProbMap[i] + 2);
                assert tmp >= 1;
                /*
                if (tmp > 1) {
                    System.out.println("(ruleProbMap[i][j] + 1) / (classPreProbMap[i] + 2)");
                    System.out.println(ruleProbMap[i][j] + " " + classPreProbMap[i]);
                }*/
                ruleProbMap[i][j] = tmp;
            }

        //laplace smoothing is not necessary here
        for (int i = 0; i < CLASS_COUNT; ++i)
            classPreProbMap[i] /= trainSet.size();

    }

    public static void main(String[] args) {
        Corpus cps = new Corpus();
        cps.setOriginBody("鑫磊玻璃那处房产抵押给我们了，别开行批了你们没东西押了");
        NaiveBayes nbc = new NaiveBayes();
        System.out.println(nbc.classify(cps));
    }

}