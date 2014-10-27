package com.xiaomi.smsspam.Utils;

public class Tokenizer {

    private static long mNativeObj;

    static
    {
        System.loadLibrary("Tokenizer");
        mNativeObj = nativeInitObject();
    }

    public String[] cut(String res){
        return nativeCut(mNativeObj, res);
    }

    public boolean inDict(String str){
        if(str.length() <= 0){
            return false;
        }
        return nativeInDict(mNativeObj, str);
    }


    public void destroy(){
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private static native long nativeInitObject();

    private static native void nativeDestroyObject(long thiz);

    private static native String[] nativeCut(long thiz, String res);

    private static native boolean nativeInDict(long thiz, String res);

    public static void main(String[] args) {

    }
}


