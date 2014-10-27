package com.xiaomi.smsspam;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.xiaomi.smsspam.Utils.Corpus;
import org.json.JSONException;
import org.json.JSONObject;

import com.xiaomi.smsspam.preprocess.RuleManager;

public class MainClass {

    public static void main(String[] args) {
        try {
            InputStream ins = new FileInputStream(Options.FilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));
            String json;

            int spamNum = 0;
            int normalNum = 0;
            int totalNum = 0;
            List<Corpus> allMsm = new ArrayList<Corpus>();

            while((json = br.readLine())!= null){
                JSONObject object = new JSONObject(json);
                String body = object.getString(Corpus.BODY);
                boolean spam = object.getBoolean(Corpus.SPAM);
                String address = object.getString(Corpus.ADDRESS);

                totalNum++;
                if(spam){
                    spamNum++;
                }
                else normalNum++;

                Corpus cps = new Corpus();
                cps.setOriginBody(body);
                cps.setIsSpam(spam);
                cps.setAddress(address);
                allMsm.add(cps);

            }

            System.out.println("totalNum:" + totalNum + "\tspamNum:" + spamNum);
            ins.close();
            br.close();

            if(Options.TEST_ALG)
            {
	            testAlg(allMsm);
	            MainClass.log("Cross Validation");
                return;
            }

            NaiveBayesManager bys = new NaiveBayesManager();
            RuleManager rmgr = new RuleManager();
            bys.startTraining(allMsm, rmgr); //贝叶斯分类器模型nbc作为bys的一个成员，在训练过程中被定义               
            MainClass.log("Training");
            
            //存储模型文件
            bys.nbc.saveModel();
            MainClass.log("Save model");
            
        	//读取模型文件
            NaiveBayes01 nbc = new NaiveBayes01();
            nbc.readModel();
            MainClass.log("Read model");
            
            //nbc = bys.nbc;
            
            //利用读取的模型进行分类，同时提取20000个样本的特征，存入feature.txt中，存为稀疏矩阵
            int normalMiss = 0;
            int normalHit = 0;
            int spamMiss = 0;
            int spamHit = 0;


            for(int i = 0; i < allMsm.size(); i++)
            {
            	Corpus cps = allMsm.get(i);
            	boolean b = nbc.classify(cps);
            	//System.out.println(String.valueOf(i));
            	
            	List<String> seg = cps.getSegments();
            	
            	if(cps.getIsSpam()){
            		if(b){ spamHit++; }
            		else{ spamMiss++; }
            	}
            	else{
            		if(b){ normalMiss++; }
            		else{ normalHit++; }
            	}
            }
            
            

            double recall = 1.0*spamHit/(spamHit+spamMiss);
            double precision = 1.0*spamHit/(spamHit+normalMiss);
            System.out.println("Recall:"+String.valueOf(recall)+"	Precision:"+String.valueOf(precision));
            MainClass.log("Classify");
            

            

            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
        System.out.println("Thread-" + id + ": " + tag + "\t time:" + cost);
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

            RuleManager rmgr = new RuleManager();

            NaiveBayesManager bys = new NaiveBayesManager();
            ArrayList<Corpus> trainMsm = trainMsms[mId];
            ArrayList<Corpus> testMsm = testMsms[mId];

			bys.startTraining(trainMsm, rmgr);


            int correct = 0;
            int spamHit = 0, spamMiss = 0, normalMiss = 0;

//            if(Options.USE_NEW_PHRASE){
//                //rmgr.process(testMsm, bys.getNewPhraseDict());
//            	rmgr.process(testMsm);
//            }else{
                rmgr.process(testMsm);
//            }
            for(int j = 0; j < testMsm.size(); ++j){
                Corpus cps = testMsm.get(j);
                boolean isSpam = cps.getIsSpam();
                boolean classifySpam = false;
					//classifySpam = bys.classify(cps);
                	classifySpam = bys.nbc.classify(cps);//用 bys 中的 NaiveBayesClassifier nbc 分类
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
            System.out.println("TestWord2Vec round[" + mId + "]:" + correct + "/" + testMsm.size() +  "\t:" + ((float)correct)/testMsm.size()
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
