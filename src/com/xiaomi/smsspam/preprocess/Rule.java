package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import com.xiaomi.smsspam.Utils.myReader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public abstract class Rule {
	
	protected static final int DEFAULT_SUB_COUNT = 1;
    protected static final int MAX_NAME_LENGTH = 30;

    protected PrintWriter modelOut;
    protected PrintWriter extractedRulesOut;
    protected myReader modelIn;

    private int startIndex = 1;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public abstract void reset();

    public abstract void updRemainingBody(Corpus cps);

    public abstract void process(Corpus cps);

    //train the parameters of the rule
    public abstract void train(List<Corpus> cpss);

    public abstract int subClassCount();

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

    public abstract void readDef(DataInputStream dataIn) throws IOException;
    public abstract void writeDef(DataOutputStream dataOut) throws IOException;
}
