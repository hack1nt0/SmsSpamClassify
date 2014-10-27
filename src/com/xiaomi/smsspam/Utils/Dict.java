package com.xiaomi.smsspam.Utils;

import java.io.*;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.IntSummaryStatistics;

/**
 * Created by root on 14-9-5.
 */
public class Dict {//unknown
    public static void changeFreq(String originFile, String destFile) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(originFile)));
        HashMap<String, Integer> originFreqs = new HashMap<>();
        String line = in.readLine();
        String tmp[];
        while (line != null) {
            tmp = line.split(" ");
            originFreqs.put(tmp[0], Integer.valueOf(tmp[1]));
            line = in.readLine();
        }
        in.close();
        in = new BufferedReader(new InputStreamReader(new FileInputStream(destFile)));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile + ".originFreq")));
        line = in.readLine();
        while (line != null) {
            tmp = line.split("\t");
            if (4 != tmp.length) {
                line = in.readLine(); continue;
            }
            int freq = originFreqs.containsKey(tmp[0]) ? originFreqs.get(tmp[0]) : Integer.valueOf(tmp[1]);
            out.write(tmp[0]);
            out.write("\t");
            out.write(String.valueOf(freq));
            out.write("\t");
            out.write(tmp[2]);
            out.write("\t");
            out.write(tmp[3]);
            out.write("\n");
            System.out.println(line);
            line = in.readLine();
        }
        in.close();
        out.close();
    }

    public static void main(String[] args) throws IOException {
        changeFreq("data/jieba.dict.utf8", "data/refined.dic");
    }
}