package com.xiaomi.smsspam.preprocess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Rule {
	
	protected static final int DEFAULT_SUB_COUNT = 1;
    protected static final int MAX_NAME_LENGTH = 30;

    protected int subClassCount(){
        return DEFAULT_SUB_COUNT;
    }

    public String getClassName(int i){
        if(i < subClassCount() && i >= 0){
            return padding(getClassName(getClass()));
        }
        return null;
    }

    protected static String getClassName(Class c){
        String name = c.getName();
        name = name.substring(name.lastIndexOf(".") + 1);
        return name;
    }

    protected static String padding(String name){
        if(name.length() < MAX_NAME_LENGTH){
            StringBuffer sb = new StringBuffer();
            int paddingCount = MAX_NAME_LENGTH - name.length();
            sb.append(name);
            while(paddingCount-- > 0){
                sb.append(" ");
            }
            return sb.toString();
        }
        return name;
    }
    
    public abstract String getName();
    public abstract void readDef(DataInputStream dataIn) throws IOException;
    public abstract void writeDef(DataOutputStream dataOut) throws IOException;
    //protected abstract boolean readRule(String s);
    
    //protected abstract boolean writeRule(OutputStream os);
}
