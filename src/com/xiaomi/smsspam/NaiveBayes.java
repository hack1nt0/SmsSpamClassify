package com.xiaomi.smsspam;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.LongFloat;
import com.xiaomi.smsspam.Utils.Utils;
import com.xiaomi.smsspam.preprocess.RuleManager;
import com.xiaomi.smsspam.preprocess.Word;
import org.openjdk.jol.samples.JOLSample_04_Inheritance;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

public class NaiveBayes extends Classifier{
	
    double binomialCPD[][];
    double multinomialCPD[][];
	double classPreProbMap[];//两类概率
	
	public static final int SPAM = Utils.SPAM;
    public static final int NORMAL = Utils.NORMAL;
    public static final int CLASS_COUNT = Utils.CLASS_COUNT;
    private Map<String, Integer> termID = new HashMap<>();
    private Map<Integer, String> idTerm = new HashMap<>();
    private double[] termSum;
    private double[] classCnt;
    int ruleCnt;

    //对Copus进行分类，垃圾邮件返回true，否则返回false
	public boolean classify(Corpus cps) {

		Set<String> terms = new HashSet<>(cps.getTokens());
		int[] rules = cps.getX();//规则特征

		//计算短信分别属于两类的概率
		double[] probs = new double[CLASS_COUNT];

		for(int classNO = 0; classNO < CLASS_COUNT; ++classNO) {
			probs[classNO] = Math.log(classPreProbMap[classNO]);

			for(int i = 1; i < cps.getX().length; ++i) {
                double tmp = binomialCPD[classNO][i + termID.size() + 1];
                if (rules[i] == 0) tmp = 1.0 - tmp;
                probs[classNO] +=  Math.log(tmp);
            }
            //MULTINOMIAL
            for (String term : terms) {
                int tid = termID.containsKey(term) ? termID.get(term) : termID.size();
                probs[classNO] += Math.log(multinomialCPD[classNO][tid]);
            }
            /*//BINOMIAL
            for (String token : termID.keySet()) {
                double tmp = binomialCPD[classNO][termID.get(token)];
                if (!terms.contains(token)) tmp = 1.0 - tmp;
                probs[classNO] +=  Math.log(tmp);
            }
            *//*
            for (String token: terms) {
                if (termID.containsKey(token)) continue;
                probs[classNO] += Math.log(1.0 / (classCnt[classNO] + 1));
            }
            */
        }
		int result = probs[SPAM] > probs[NORMAL] * Options.SPAM_RATIO_THRESHOLD ? SPAM : NORMAL;
        return result == SPAM;
	}


    public void train(List<Corpus> trainSet) {
        if (trainSet == null || trainSet.size() == 0) {
            throw new RuntimeException("trainSet is null");
        }
        ruleCnt = trainSet.get(0).getX().length;
        termID = Word.getGlossary() != null ? Word.getGlossary() : termID;
        for (String token: termID.keySet()) idTerm.put(termID.get(token), token);

        classPreProbMap = new double[CLASS_COUNT];
        binomialCPD = new double[CLASS_COUNT][ruleCnt + termID.size() + 1];
        multinomialCPD = new double[CLASS_COUNT][termID.size() + 1];
        termSum = new double[CLASS_COUNT];
        classCnt = new double[CLASS_COUNT];

        for (Corpus cps: trainSet) {
            List<String> terms = cps.getTokens();//分词结果
            int classNO = cps.getIsSpam() ? SPAM : NORMAL;
            ruleCnt = cps.getX().length;
            ++classCnt[classNO];

            for (int i = 0; i < terms.size(); ++i) {
                String term = terms.get(i);
                if (!termID.containsKey(term)) {
                    System.out.println(term);
                    continue;
                }
                multinomialCPD[classNO][termID.get(term)] += 1.0;
                binomialCPD[classNO][termID.get(term)] += 1.0;
                termSum[classNO]++;
            }

            int[] rules = cps.getX();
            for (int i = 0; i < rules.length; ++i) {
                binomialCPD[classNO][i + termID.size() + 1] += rules[i] > 0 ? 1 : 0;
            }
        }

        for (int i = 0; i < Utils.CLASS_COUNT; ++i) {
            for (int j = 0; j <= termID.size(); ++j) {
                multinomialCPD[i][j] = (multinomialCPD[i][j] + 1) / (termSum[i] + termID.size() + 1);
                binomialCPD[i][j] = (binomialCPD[i][j] + 1) / (classCnt[i] + 2);
            }
        }
        for (int i = 0; i < CLASS_COUNT; ++i)
            for (int j = 1; j < ruleCnt; ++j) {
                binomialCPD[i][j + termID.size() + 1] = (binomialCPD[i][j + termID.size() + 1] + 1) / (classCnt[i] + 2);
            }

        //laplace smoothing is not necessary here
        for (int i = 0; i < CLASS_COUNT; ++i)
            classPreProbMap[i] = classCnt[i] / trainSet.size();

        saveModel("data/NB.model");

    }

    @Override
    public void saveModel(String filePath) {
        try {
            PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(filePath)));
            out.println(CLASS_COUNT + "\t" + termID.size() + "\t" + ruleCnt);
            out.println("CLASSPREPROB" + "\t" + classPreProbMap[SPAM] + "\t" + classPreProbMap[NORMAL]);
            for (int i = 0; i < termID.size() + 1; ++i)
                out.println((i < termID.size() ? idTerm.get(i) : "BLACKHOLE") + "\t" + binomialCPD[SPAM][i] + "\t" + binomialCPD[NORMAL][i]);
            List<String> ruleNames = RuleManager.getRuleNames();
            for (int i = 1; i < ruleNames.size(); ++i)
                out.println(ruleNames.get(i) + "\t" + binomialCPD[SPAM][termID.size() + 1 + i] + "\t" + binomialCPD[NORMAL][termID.size() + 1 + i]);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readModel(String filePath) {

    }
}