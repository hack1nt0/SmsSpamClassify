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
        String contrastFilePath = "data/tokenizerContrast.txt.1";
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(testFilePath)));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(contrastFilePath)));
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            out.println(line);
            out.println(Arrays.asList(Tokenizer.cut(line)));
            out.println(Arrays.asList(myTokenizer.getTokens(line)));
            out.println();
        }
        out.close();
        in.close();
    }
}


