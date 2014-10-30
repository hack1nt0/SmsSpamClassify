package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Splits extends RulePrevious {
    //"\\\\" modelIn RE means "\" modelIn raw text TODO
    private static String[] REs;
    private int[] hits;
    private Pattern[] autoMachine;

    private List<String> curSplits;
    private List<String> validSplits; //TODO

    public Splits() {
        try {
            extractedRulesOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/extractedSplits.txt")));
            modelOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/validSplits.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        curSplits = new ArrayList<>();
        validSplits = new ArrayList<>();
        REs = new String[]{"( ){3,}", "(\\t){1,}", "([\\n\\r])+"};
        autoMachine = new Pattern[REs.length];
        for (int i = 0; i < autoMachine.length; ++i) autoMachine[i] = Pattern.compile(REs[i]);
        hits = new int[REs.length];
    }

    @Override
    public void reset(){
        Arrays.fill(hits, 0);
    }

    @Override
    public void updRemainingBody(Corpus cps) {
        for (int i = 0; i < autoMachine.length; ++i) {
            List<String> nsegs = new ArrayList<>();
            for (String line : cps.getRemainingBody()) {
                Matcher matcher = autoMachine[i].matcher(line);
                while (matcher.find())
                    curSplits.add(matcher.group(0).replaceAll(REs[i], REs[i] + ":" + (matcher.end() - matcher.start())));
                String[] segs = line.split(REs[i]);
                nsegs.addAll(Arrays.asList(segs));
                hits[i] += segs.length - 1;
            }
            cps.setRemainingBody(nsegs);
        }
        //TODO upd the curSplits
    }

    @Override
    public void process(Corpus cps) {
        curSplits.clear();
        updRemainingBody(cps);
        writeCurRules(extractedRulesOut, curSplits);
        for (int i = 0; i < REs.length; ++i) {
            cps.getX()[this.getStartIndex() + i] = hits[i] > 0 ? 1 : 0;
        }
    }

    @Override
    public int subClassCount() {
        return REs.length;
    }

    @Override
    public void train(List<Corpus> cpss) {
        /*
        for (int S = 0; S < 1<<REs.length; ++S) {
            //TODO find the optimistic S
        }*/
        curSplits.clear();
        for (Corpus cps: cpss) {
            updRemainingBody(cps);
        }
        validSplits.addAll(curSplits);
        writeCurRules(modelOut, validSplits);
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
