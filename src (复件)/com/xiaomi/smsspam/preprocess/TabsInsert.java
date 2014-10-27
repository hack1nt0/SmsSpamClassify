package com.xiaomi.smsspam.preprocess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TabsInsert extends RulePrevious {

	@Override
    protected List<String> process(String str) {
        if(null == str){
            return null;
        }
        ArrayList<String> ret = new ArrayList<String>();
        if(str.indexOf('\t') != -1){
            mHit = true;
        }
        ret.add(str.replaceAll("\t", ""));
        return ret;
    }

    @Override
    public boolean doFitting(int[] vals, int start) {
        if(mHit){
            vals[start]++;
            return true;
        }
        return false;
    }

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "TabsInsert";
	}

	@Override
	public void readDef(DataInputStream dataIn) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
