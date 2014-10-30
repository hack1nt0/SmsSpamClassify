package com.xiaomi.smsspam.preprocess;

/**
 * Created by dy on 14-10-30.
 */
import java.util.ArrayList;

import com.xiaomi.common.Log;
import com.xiaomi.smsner.EntityType;
import com.xiaomi.smsner.NumberRecognition;
import com.xiaomi.smsner.RecognitionResult;


public class NER {
    private static final String TAG = "Program";

    public static void main(String[] args) throws Exception {

        if(!NumberRecognition.initial()){
            return;
        }
        NumberRecognition.addRecognitionTask(EntityType.BankCardNumber);
        NumberRecognition.addRecognitionTask(EntityType.ExpressNumber);
        NumberRecognition.addRecognitionTask(EntityType.VerificationCode);
        NumberRecognition.addRecognitionTask(EntityType.PhoneNumber);
        NumberRecognition.addRecognitionTask(EntityType.URL);
        NumberRecognition.addRecognitionTask(EntityType.Time);

        for(int i=0;i<10;i++){
            String target="您好！快件688532824006单标地址错误且无法联系到您，无法派送，见字请致电4008-111-111处理。10:09【顺丰速运】";
            NumberRecognition nr=new NumberRecognition(target);
            ArrayList<RecognitionResult> recognitionResults=nr.recognize();
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
    }
}
