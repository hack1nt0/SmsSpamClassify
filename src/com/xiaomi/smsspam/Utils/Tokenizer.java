package com.xiaomi.smsspam.Utils;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.*;
import java.util.Arrays;

public class Tokenizer {

    private static long mNativeObj;
    private static MyTokenizer myTokenizer;

    static
    {
        System.loadLibrary("Tokenizer");
        mNativeObj = nativeInitObject();
        myTokenizer = new MyTokenizer("data/jieba.dict.utf8.sorted", "data/hmm_model.utf8");
    }

    public static String[] cut(String text){
        return nativeCut(mNativeObj, text);
    }

    public static boolean inDict(String token){
        if(token.length() <= 0){
            return false;
        }
        return nativeInDict(mNativeObj, token);
    }


    public static void destroy(){
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private static native long nativeInitObject();

    private static native void nativeDestroyObject(long thiz);

    private static native String[] nativeCut(long thiz, String res);

    private static native boolean nativeInDict(long thiz, String res);

    public static void main(String[] args) throws IOException {

        String testFilePath = "data/testTokenizer.txt";
        String contrastFilePath1 = "data/tokenizerContrast.txt.1";
        String contrastFilePath2 = "data/tokenizerContrast.txt.2";
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(testFilePath)));
        PrintWriter out1 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(contrastFilePath1)));
        PrintWriter out2 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(contrastFilePath2)));
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            out1.println(line);
            out1.println(Arrays.asList(Tokenizer.cut(line)));
            out1.println();
            out2.println(line);
            out2.println(Arrays.asList(myTokenizer.getTokens(line)));
            out2.println();
        }
        out1.close();
        out2.close();
        in.close();
    }
}


