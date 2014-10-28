package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabsSerial extends RulePrevious {
    //"\\\\" in RE means "\" in raw text TODO
    private static String[] REs;
    private int[] hits;

    public TabsSerial() {
        REs = new String[]{"( ){3,}", "(\\\\*[\tt]){1}", "(\\\\*[\tt]){2,}", "(\\\\*[\n\rrn]){1,}"};
        hits = new int[REs.length];
    }

    @Override
    public void reset(){
        Arrays.fill(hits, 0);
    }

    @Override
    public boolean fit(Corpus cps, int startIndex) {
        boolean res = false;
        for (int i = 0; i < REs.length; ++i) {
            cps.getRulesPreHits()[startIndex + i] = hits[i] > 0 ? 1 : 0;
            res |= hits[i] > 0;
        }
        return res;
    }

    @Override
    protected List<String> process(String str) {
        StringBuffer sb = new StringBuffer("");
        for (int i = 0; i < REs.length; ++i) {
            String[] segs = str.split(REs[i]);
            for (String seg: segs) sb.append(seg);
            hits[i] += segs.length - 1;
        }
        return new ArrayList<>(Arrays.asList(sb.toString()));
    }

    @Override
    public int subClassCount() {
        return REs.length;
    }

    @Override
    public void train(List<Corpus> cpss) {
        for (int S = 0; S < 1<<REs.length; ++S) {
            //TODO find the optimistic S
        }
    }

    @Override
	public String getName() {
		return "TabsSerial";
	}

	@Override
	public void readDef(DataInputStream dataIn) throws IOException {

	}

	@Override
	public void writeDef(DataOutputStream dataOut) throws IOException {

	}

    public static void main(String[] args) {
        String text = "a\\t\tb";

        String[] segs = text.split(REs[0]);
        for (String seg: segs)
            System.out.println(seg + "$");
    }

}