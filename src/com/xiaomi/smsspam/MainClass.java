package com.xiaomi.smsspam;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.preprocess.Word;
import org.json.JSONObject;

import com.xiaomi.smsspam.preprocess.RuleManager;

public class MainClass {

    public static List<Corpus> readSMS(String file){
        List<Corpus> allMsm = new ArrayList<Corpus>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            int spamNum = 0;
            int totalNum = 0;
            while(true){
                String line = in.readLine();
                if (line == null) break;
                JSONObject jsonObject = new JSONObject(line);
                String body = jsonObject.getString(Corpus.BODY);
                boolean isSpam = jsonObject.getBoolean(Corpus.SPAM);
                String address = jsonObject.getString(Corpus.ADDRESS);
                totalNum++;
                if(isSpam){
                    spamNum++;
                }
                allMsm.add(new Corpus(body, isSpam, address));
            }
            System.out.println("totalNum:" + totalNum + "\tspamNum:" + spamNum);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allMsm;
    }

    public static void main(String[] args) {
        List<Corpus> trainData = readSMS("data/all_processed.txt.dist.filtled");
        //cross validation
        if(Options.TEST_ALG) {
            testAlg(trainData);
            MainClass.log("Cross Validation");
            return;
        }

        //transfer validation
        RuleManager ruleManager = new RuleManager();
        ruleManager.train(trainData);
        ruleManager.process(trainData);
        Classifier NB = new NaiveBayes();
        NB.train(trainData);
        MainClass.log("Training");

        int normalMiss = 0;
        int normalHit = 0;
        int spamMiss = 0;
        int spamHit = 0;
        List<Corpus> testData = readSMS("data/test_data_14.10.17.txt");
        for(int i = 0; i < testData.size(); i++) {
            Corpus cps = testData.get(i);
            ruleManager.process(cps);
            boolean b = NB.classify(cps);
            if(cps.getIsSpam()) {
                if(b){ spamHit++; }
                else{ spamMiss++; }
            }
            else{
                if(b) { normalMiss++; }
                else{ normalHit++; }
            }
        }
        double recall = 1.0*spamHit/(spamHit+spamMiss);
        double precision = 1.0*spamHit/(spamHit+normalMiss);
        System.out.println("spamHit:" + spamHit + "\tspamMiss:" + spamMiss + "\tnormalHit:" + normalHit + "\tnormalMiss:" + normalMiss);
        System.out.println("Recall:"+String.valueOf(recall)+"    Precision:"+String.valueOf(precision));
        MainClass.log("Classify");

        //save model
        //NB.saveModel("data/NB.model");
    }

    static double[] recalls = new double[Options.THREAD_COUNT];
    static double[] presisions = new double[Options.THREAD_COUNT];
    static boolean[] RunStatus = new boolean[Options.THREAD_COUNT];  // false is running, true is stopped.
    public static long LastLogTime[] = new long[Options.THREAD_COUNT];
    public static long ThreadIds[] = new long[Options.THREAD_COUNT];

    static ArrayList<Corpus>[] trainMsms = new ArrayList[Options.THREAD_COUNT];
    static ArrayList<Corpus>[] testMsms = new ArrayList[Options.THREAD_COUNT];

    private static void disorganize(List<Corpus> allMsm){
        final int LEN = allMsm.size();
        for(int i = 0; i < LEN; ++i){
            Corpus tmp = allMsm.get(i);
            int r = (int)(Math.random() * LEN);
            if(r >= LEN){
                r = LEN - 1;
            }
            allMsm.set(i, allMsm.get(r));
            allMsm.set(r, tmp);
        }
    }

    public static void testAlg(List<Corpus> allMsm){
        if(Options.DO_DISORGANIZING){
            disorganize(allMsm);
        }

        for(int i = 0; i < Options.THREAD_COUNT; ++i){
            trainMsms[i] = new ArrayList<Corpus>();
            testMsms[i] = new ArrayList<Corpus>();
            for(int j = 0; j < allMsm.size(); ++j){
                if(j % Options.COUNT == i){
                    testMsms[i].add(allMsm.get(j).clone());
                }else{
                    trainMsms[i].add(allMsm.get(j).clone());
                }
            }
            RunStatus[i] = false;
            TestThread ttd = new TestThread(i);
            if(Options.THREAD_COUNT == 1){
                ttd.run();
            }else{
                ttd.start();
            }
        }
    }

    public static void log(String tag){
        long threadId = Thread.currentThread().getId();
        int id = 0;
        for(int i = 0; i < ThreadIds.length; ++i){
            if(threadId == ThreadIds[i]){
                id = i;
                break;
            }
        }
        long time = System.currentTimeMillis();
        long cost = time - LastLogTime[id];
        System.out.println("Thread-" + id + ": " + tag + "\t time: " + cost + "ms");
        LastLogTime[id] = time;
    }

    static class TestThread extends Thread{
        int mId;
        public TestThread(int id){
            mId = id;
        }
        public void run() {
            ThreadIds[mId] = Thread.currentThread().getId();
            LastLogTime[mId] = System.currentTimeMillis();

            RuleManager ruleManager = new RuleManager();

            Classifier NB = new NaiveBayes();
            ArrayList<Corpus> trainMsm = trainMsms[mId];
            ArrayList<Corpus> testMsm = testMsms[mId];

            ruleManager.process(trainMsm);
            NB.train(trainMsm);

            int correct = 0;
            int spamHit = 0, spamMiss = 0, normalMiss = 0;
            for(int j = 0; j < testMsm.size(); ++j){
                Corpus cps = testMsm.get(j);
                boolean isSpam = cps.getIsSpam();
                ruleManager.process(cps);
                boolean classifySpam = NB.classify(cps);
                if(isSpam == classifySpam){
                    correct++;
                }
                if(isSpam){
                    if(classifySpam){
                        spamHit++;
                    }else{
                        spamMiss++;
                    }
                }else{
                    if(classifySpam){
                        normalMiss++;
                    }
                }
            }

            MainClass.log("Step 5");
            recalls[mId] = ((double)spamHit)/(spamHit + spamMiss);
            presisions[mId] = ((double)spamHit)/(spamHit + normalMiss);
            System.out.println("Test round[" + mId + "]:" + correct + "/" + testMsm.size() +  "\t:" + ((float)correct)/testMsm.size()
                    + " \tRecall:" + recalls[mId] + " \tPrecision:" + presisions[mId]);


            System.out.println("\tspamHit:" + spamHit
                    + " \tspamMiss:" + spamMiss
                    + " \tnormalHit:" + (testMsm.size() - normalMiss- spamHit - spamMiss)
                    + " \tnormalMiss:" + normalMiss);

            RunStatus[mId] = true;

            boolean allStopped = true;
            for(boolean stopped : RunStatus){
                if(!stopped){
                    allStopped = false;
                    break;
                }
            }

            if(allStopped){
                double averageRecall = 0.0;
                double averagePresision = 0.0;
                for(int i = 0; i < Options.THREAD_COUNT; ++i){
                    averageRecall += recalls[i];
                    averagePresision += presisions[i];
                }
                averageRecall /= Options.THREAD_COUNT;
                averagePresision /= Options.THREAD_COUNT;
                System.out.println("-------------------Average value--------------------");
                System.out.println("Recall:" + averageRecall + " \tPresision:" + averagePresision);
            }
        }
    }
}
