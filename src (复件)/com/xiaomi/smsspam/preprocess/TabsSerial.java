package com.xiaomi.smsspam.preprocess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabsSerial extends RulePrevious {

	private static final int MIN_COUNT = 2;

    private List<Integer> mCounts = new ArrayList<Integer>();

    @Override
    public boolean doFitting(int[] vals, int start) {
        if(mCounts.size() > 0){
            vals[start]++;
            return true;
        }
        return false;
    }

    protected void reset(){
        super.reset();
        mCounts.clear();
    }

    @Override
    protected List<String> process(String str) {
        if(null == str){
            return null;
        }
        ArrayList<String> ret = new ArrayList<String>();
        str = str.replaceAll("\\t", "\t");
                
        int startPos = -1, lastPos = 0;
        boolean lastIsTab = false;
        for(int i = 0; i < str.length(); ++i){
            if(str.charAt(i) == '\t'){
                if(!lastIsTab){
                    startPos = i;
                    lastIsTab = true;
                }
            }else{
                if(lastIsTab){
                    if(i - startPos > MIN_COUNT){
                        mCounts.add(i - startPos);
                        ret.add(str.substring(lastPos, startPos));
                        lastPos = i;
                        startPos = -1;
                    }
                    lastIsTab = false;
                }
            }
        }
        if(startPos > 0 && str.length() - startPos >= MIN_COUNT){
            mCounts.add(str.length() - startPos);
            ret.add(str.substring(lastPos, startPos));
        }else{
            ret.add(str.substring(lastPos));
        }
        return ret;
    }
    
    @Override
	public String getName() {
		// TODO Auto-generated method stub
		return "TabsSerial";
	}

	@Override
	public void readDef(DataInputStream dataIn) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {
		// TODO Auto-generated method stub
		
	}

//    public static void main1(String[] args) {
//        String[] strs = {
//                "3G视界”新闻推荐：女子打死3岁半儿子称不忏悔。更多内容等您看，所有视频均免流量！http://sh.wasu.cn/826\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
//                "drdgdg dfg 3\t\t\t闻推荐：女子打死3岁半儿子称不忏悔。更多内容等您看，所有视频均免流\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t",
//
//        };
//
//        TabsSerial tb = new TabsSerial();
//
//        for(String s : strs){
//            System.out.println("------------------------------------------------------------------");
//            System.out.println("[sms]:" + s);
//            List<String> ret = tb.process(s);
//            System.out.println("[hit]:" + tb.hit());
//            for(String ss : ret){
//                System.out.println(ss);
//            }
//        }
//    }
}
