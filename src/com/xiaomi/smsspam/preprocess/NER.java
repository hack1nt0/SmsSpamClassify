package com.xiaomi.smsspam.preprocess;

/**
 * Created by dy on 14-10-30.
 */
import com.xiaomi.common.Log;
import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsunderstand.RecognitionResult;
import com.xiaomi.smsunderstand.SMSUnderstand;

import java.io.*;
import java.util.*;


public class NER extends Rule {
    private static final String TAG = "Program";
    SMSUnderstand nr;
    List<RecognitionResult> extractedNEs;

    public NER() {
        if(!SMSUnderstand.initial()){
            return;
        }
        nr = new SMSUnderstand();
        try {
            extractedRulesOut = new PrintWriter(new FileOutputStream("data/extractedNEs.txt"));
            extractedNEs = new ArrayList<>();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        long beforeM = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        if(!SMSUnderstand.initial()){
            return;
        }

        SMSUnderstand nr = new SMSUnderstand();

        String target="您好！快件688532824006单标地址错误且无法联系到您，无法派送，见字请致电4008-111-111处理。10:09【顺丰速运】";
        target = "58919101/02/03.tell15052009552";

        ArrayList<RecognitionResult> recognitionResults=nr.recognize(target);

        long afterM = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double usedM = (afterM - beforeM) / 1024.0 / 1024;
        System.out.println("Consumed Mem: " + usedM + "MB");

        for (RecognitionResult recognitionResult : recognitionResults) {
            Log.i(TAG, "实体开始位置："+recognitionResult.getStartPosition());
            Log.i(TAG, "实体结束位置："+recognitionResult.getEndPosition());
            Log.i(TAG, "规则化的结果："+recognitionResult.getRegularizationResult());
            Log.i(TAG, "实体的类型："+recognitionResult.getEntityType());
            Log.i(TAG, "实体的参数："+recognitionResult.getParameter());
            Log.i(TAG, "实体识别结果置信度："+recognitionResult.getConfidence());
            System.out.println();
        }
    }

    @Override
    public void reset() {
        extractedNEs.clear();
    }

    @Override
    public void updRemainingBody(Corpus cps) {
    }

    @Override
    public void process(Corpus cps) {
        try {
            ArrayList<RecognitionResult> nes =nr.recognize(cps.getOriginalBody());
            extractedNEs = nes;
            extractedRulesOut.println(extractedNEs);
            for (RecognitionResult n: nes) cps.getX()[this.getStartIndex() + n.getEntityType().ordinal()] = 1;

            List<String> remainBody = new ArrayList<>();
            Collections.sort(extractedNEs, new Comparator<RecognitionResult>() {
                @Override
                public int compare(RecognitionResult o1, RecognitionResult o2) {
                    if (o1.getStartPosition() != o2.getStartPosition())
                        return o1.getStartPosition() - o2.getStartPosition();
                    return o1.getEndPosition() - o2.getEndPosition();
                }
            });
            int len = cps.getOriginalBody().length();
            for (int i = 0, k = 0; i < len; ++i) {
                int j = i;
                while (j < len && (k >= extractedNEs.size() || j < extractedNEs.get(k).getStartPosition())) ++j;
                remainBody.add(cps.getOriginalBody().substring(i, j));
                i = k < extractedNEs.size() ? extractedNEs.get(k).getEndPosition() : j;
                ++k;
            }
            cps.setRemainingBody(remainBody);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void train(List<Corpus> cpss) {
        try {
            for (Corpus cps: cpss) {
                ArrayList<RecognitionResult> nes = nr.recognize(cps.getOriginalBody());
                extractedNEs = nes;
                List<String> remainBody = new ArrayList<>();
                Collections.sort(extractedNEs, new Comparator<RecognitionResult>() {
                    @Override
                    public int compare(RecognitionResult o1, RecognitionResult o2) {
                        if (o1.getStartPosition() != o2.getStartPosition())
                            return o1.getStartPosition() - o2.getStartPosition();
                        return o1.getEndPosition() - o2.getEndPosition();
                    }
                });
                int len = cps.getOriginalBody().length();
                for (int i = 0, k = 0; i < len; ++i) {
                    int j = i;
                    while (j < len && (k >= extractedNEs.size() || j < extractedNEs.get(k).getStartPosition())) ++j;
                    remainBody.add(cps.getOriginalBody().substring(i, j));
                    i = k < extractedNEs.size() ? extractedNEs.get(k).getEndPosition() : j;
                    ++k;
                }
                cps.setRemainingBody(remainBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
        };
    }

    @Override
    public int subClassCount() {
        return 15;
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {

    }
}
