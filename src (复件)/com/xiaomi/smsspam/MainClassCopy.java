package com.xiaomi.smsspam;

import com.xiaomi.smsspam.Utils.Corpus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class MainClassCopy {

    public static void main(String[] args) {
        try {
            InputStream ins = new FileInputStream(Options.FilePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));
            String json;

            int spamNum = 0;
            int totalNum = 0;
            List<Corpus> allMsm = new ArrayList<Corpus>();
            List<Integer> labelList = new ArrayList<Integer>();

            while ((json = br.readLine()) != null) {
                JSONObject object = new JSONObject(json);
                String body = object.getString(Corpus.BODY);
                boolean spam = object.getBoolean(Corpus.SPAM);
                String address = object.getString(Corpus.ADDRESS);

                totalNum++;
                if (spam) {
                    spamNum++;
                    labelList.add(1);
                } else
                    labelList.add(0);

                Corpus cps = new Corpus();
                cps.setOriginBody(body);
                cps.setIsSpam(spam);
                cps.setAddress(address);
                allMsm.add(cps);

            }

            System.out.println("totalNum:" + totalNum + "\tspamNum:" + spamNum);
            ins.close();
            br.close();

/*            FileOutputStream featureOut = new FileOutputStream(Options.FEATURES);
            FileOutputStream labelOut = new FileOutputStream(Options.LABELS); */
            //validate
            NaiveBayes nbc = new NaiveBayes();
            double recall = 0.0;
            double precision = 0.0;

            for (int itr = 0; itr < Options.CROSS_COUNT; ++itr) {
                //train
                List<Corpus> trainSet = new ArrayList<Corpus>();
                for (int i = 0; i < allMsm.size(); ++i) {
                    if (i * Options.CROSS_COUNT / allMsm.size() == itr)
                        continue;
                    trainSet.add(allMsm.get(i));
                }

                nbc.trainModel(trainSet);

                int normalMiss = 0;
                int normalHit = 0;
                int spamMiss = 0;
                int spamHit = 0;
                //classify
                for (int i = 0; i < allMsm.size(); ++i) {
                    if (i * Options.CROSS_COUNT / allMsm.size() != itr)
                        continue;
                    Corpus cps = allMsm.get(i);

                    List<Boolean> boolList = new ArrayList<Boolean>();
                    List<Integer> fpList = new ArrayList<Integer>();
                    List<Integer> fnList = new ArrayList<Integer>();

                    boolean b = nbc.classify(cps);
                    boolList.add(b);

                    if (cps.getIsSpam()) {
                        if (b) {
                            spamHit++;
                        } else {
                            System.out.println("spamMiss");
                            System.out.println(cps.getOriginBody());
                            System.out.println(cps.getSegments());
                            spamMiss++;
                            fnList.add(i + 1);
                        }
                    } else {
                        if (b) {
                            System.out.println("normalMiss");
                            System.out.println(cps.getOriginBody());
                            System.out.println(cps.getSegments());
                            normalMiss++;
                            fpList.add(i + 1);
                        } else {
                            normalHit++;
                        }
                    }
                }

                recall += 1.0 * spamHit / (spamHit + spamMiss);
                precision += 1.0 * spamHit / (spamHit + normalMiss);
                System.out.println("itr " + itr + ": " + "Recall " + recall / (itr + 1) + " Precision " + precision / (itr + 1));

            }
            recall /= Options.CROSS_COUNT;
            precision /= Options.CROSS_COUNT;
            System.out.println("Recall:"+String.valueOf(recall)+"	Precision:"+String.valueOf(precision));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
