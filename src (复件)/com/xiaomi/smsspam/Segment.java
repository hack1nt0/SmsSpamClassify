package com.xiaomi.smsspam;

public class Segment {


	static
    {
        System.loadLibrary("WordsSegment");
    }
    private long mNativeObj = 0;

    public void init(){
    	MainClass.log("Start");
        System.out.println("init start");
        mNativeObj = nativeInitObject();
        System.out.println("init end. value:" + mNativeObj);
        MainClass.log("Init");
    }

    public void destroy(){
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
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

    private static native long nativeInitObject();

    private static native void nativeDestroyObject(long thiz);

    private static native String[] nativeCut(long thiz, String res);

    private static native boolean nativeInDict(long thiz, String res);

    public static void main(String[] args) {
        System.out.println(System.getProperty("java.library.path"));

    }
}
