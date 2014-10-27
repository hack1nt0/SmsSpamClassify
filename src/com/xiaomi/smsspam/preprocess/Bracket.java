package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;
import org.codehaus.jettison.json.JSONException;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 14-10-13.
 */
public class Bracket extends  RulePrevious{
    private static int MAX_SUBCLASS_NUM;
    private static String RE;
    private static String brackets;
    private static Set<String> Xs;
    private static Map<String, Integer> validXs;

    public Bracket() {
        Xs = new HashSet<>();
        brackets = "()[]{}<>【】（）《》『』";
        RE = "";
        for (int i = 0; i < brackets.length(); i += 2) {
            String bl = brackets.charAt(i) + "", br = brackets.charAt(i + 1) + "";
            if (i < 6) {bl = "\\" + bl; br = "\\" + br;}
            if (i > 0) RE += "|";
            RE += bl + "[^" + bl + "^" + br + "]+" + br;
        }
        RE = "^(" + RE + ")|(" + RE + ")$";
        //RE = "\\([^\\(^\\)]+\\)$";
        validXs = new HashMap<>();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/brackets_entropy1.txt")));
            for (int id = 0;; ++id) {
                String line = in.readLine();
                if (line == null) break;
                ++MAX_SUBCLASS_NUM;
                String[] XH = line.split("\t");
                validXs.put(XH[0], id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Map.Entry<String, Double>> Hs;

    @Override
    public int subClassCount() {
        return MAX_SUBCLASS_NUM;
    }

    @Override
    public void train(List<Corpus> cpss) {
        /*
       BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/all_processed.txt.dist.filtled")));
        //BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        PrintWriter out1 = new PrintWriter(new ObjectOutputStream(new FileOutputStream("data/brackets_true.txt")));
        PrintWriter out2 = new PrintWriter(new ObjectOutputStream(new FileOutputStream("data/brackets_false.txt")));
        */
        int head = 0, tail = 0;
        Map<String, Integer> trues = new HashMap<>(); Map<String, Integer> falses = new HashMap<>();
        int tn = 0, fn = 0; // N * M
        for (Corpus cps: cpss) {
            Map<String, Integer> cnts = cps.getIsSpam() ? trues : falses;
            //PrintStream out = System.out;
            List<String> segs = cps.getRefinedSegments();
            if (segs.size() == 0) continue;
            for (String x: filter(segs.get(0)))
                cnts.put(x, cnts.containsKey(x) ? cnts.get(x) + 1 : 1);
            for (String x: filter(segs.get(segs.size() - 1)))
                cnts.put(x, cnts.containsKey(x) ? cnts.get(x) + 1 : 1);
            if (cps.getIsSpam()) ++tn; else ++fn;
        }
        Hs = getEntropies(trues, falses, tn, fn);
        Collections.sort(Hs, (o1, o2) -> {
            if (Math.abs(o2.getValue() - o1.getValue()) < 1e-9) return 0;
            if (o2.getValue() > o1.getValue()) return 1;
            return -1;
        });
    }

    @Override
    public void reset() {
        Xs.clear();
    }

    //train offline
    public static void main(String[] args) throws IOException, JSONException {
        //train();
        String t = "(12)";
        for (String x: filter(t)) System.out.println(x);
    }

    public void train() throws IOException, JSONException{

    }

    private static Set<String> filter(String body) {
        Pattern pattern = Pattern.compile(RE);
        Matcher matcher = pattern.matcher(body);
        Set<String> Xs = new HashSet<>();
        while (matcher.find())
            for (int i = 0; i <= matcher.groupCount(); ++i) {
                if (matcher.group(i) == null) continue;
                Xs.add(matcher.group(i));
            }
        return Xs;
    }

    private static List<Map.Entry<String, Double>> getEntropies(Map<String, Integer> trues, Map<String, Integer> falses, double tn, double fn) {
        List<Map.Entry<String, Double>> res = new ArrayList<>();
        Double N = tn + fn;
        Set<String> allX = new HashSet<>(); for (String x: trues.keySet()) allX.add(x); allX.addAll(falses.keySet()); //??
        /*
        Set<Integer> set2 = new HashSet<Integer>(){{
            add(1);
            add(2);
            add(3);
        }};
        */
        for (String X: allX) {
            double txn = trues.containsKey(X) ? trues.get(X) : 0;
            double fxn = falses.containsKey(X) ? falses.get(X) : 0;
            Double H = 0.0;
            //0 0
            H += tn - txn == 0 ? 0 : (tn - txn) / N * Math.log((tn - txn) / (N - txn - fxn)); // ??/Math.log(2)
            //0 1
            H += fn - fxn == 0 ? 0 : (fn - fxn) / N * Math.log((fn - txn) / (N - txn - fxn));
            //1 0
            H += txn == 0 ? 0 : txn / N * Math.log(txn / (txn + fxn));
            //1 1
            H += fxn == 0 ? 0 : fxn / N * Math.log(fxn / (txn + fxn));
            res.add(new AbstractMap.SimpleImmutableEntry<String, Double>(X, H));
        }
        return res;
    }

    @Override
    public boolean fit(Corpus cps, int start) {
        boolean has = false;
        for (String x: Xs) {
            if (!validXs.containsKey(x)) continue;
            has = true;
            cps.getRulesPreHits()[start + validXs.get(x)] = 1;
        }
        return has;
    }

    @Override
    protected List<String> process(String str) {
        List<String> res = new ArrayList<>();
        //while (true) {
            Set<String> Xs = filter(str);
         //   if (Xs.size() == 0) break;
            this.Xs.addAll(Xs);
            for (String x: Xs) {
                if (str.startsWith(x)) str = str.substring(x.length());
                if (str.endsWith(x)) str = str.substring(0, str.length() - x.length());
            }
        //}
        //System.out.println(str);
        res.add(str);
        return res;
    }

    @Override
    public String getName() {
        return "Bracket";
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {
        FileWriter out3 =  new FileWriter("data/brackets_entropy1.txt");
        for (Map.Entry e: Hs) out3.write(e.getKey() + "\t" + e.getValue() + "\n");
        out3.close();
    }
}
