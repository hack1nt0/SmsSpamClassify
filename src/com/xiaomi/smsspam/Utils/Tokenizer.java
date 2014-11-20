package com.xiaomi.smsspam.Utils;

public class Tokenizer {

    private static long mNativeObj;

    static
    {
        long before = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.loadLibrary("Tokenizer");
        mNativeObj = nativeInitObject();
        long after = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        double usedMB = (after - before) / 1024.0 / 1024;
        System.out.println("tokenizer used mem: " + usedMB);
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


