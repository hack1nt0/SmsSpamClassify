package com.xiaomi.smsspam.Utils;

import java.io.*;
import java.util.*;

/**
 * Created by dy on 14-10-25.
 */
public class Strings {

    static class ACAutomation {

        class Node {
            char c;
            int type;// 1: end, 0: else
            int depth;
            Map<Character, Node> childs;
            Node fa, fail, lastPattern;
            int patternId;

            Node(int type, char c, Node fa) {
                this.type = type;
                this.depth = -1;
                this.c = c;
                this.childs = new HashMap<>();
                this.fa = fa == null ? this : fa;
                this.fail = this;
                this.lastPattern = this;
            }

            public boolean isRoot() {
                return fa == this;
            }
        }

        Node root;

        public ACAutomation(List<String> ss) {
            root = new Node(0, '\0', root);
            for (int i = 0; i < ss.size(); ++i) insert(ss.get(i), i);
            Queue<Node> Q = new LinkedList<>();
            Q.add(root);
            while (!Q.isEmpty()) {
                Node cur = Q.poll();
                cur.depth = cur.fa.depth + 1;
                //fail
                if (cur == root || cur.fa == root) {
                    cur.fail = root;
                } else {
                    cur.fail = cur.fa.fail;
                    while (cur.fail != root && !cur.fail.childs.containsKey(cur.c)) cur.fail = cur.fail.fail;
                    if (cur.fail.childs.containsKey(cur.c)) cur.fail = cur.fail.childs.get(cur.c);
                }
                //lastPattern
                cur.lastPattern = cur.fail.type == 1 ? cur.fail : cur.fail.lastPattern;
                for (Node chd : cur.childs.values()) Q.add(chd);
            }
        }


        public void insert(String s, int index) {
            Node cur = root;
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (!cur.childs.containsKey(c))
                    cur.childs.put(c, new Node(0, c, cur));
                cur = cur.childs.get(c);
            }
            cur.type = 1;
            cur.patternId = index;
        }

        public boolean find(String s) {
            Node cur = root;
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (!cur.childs.containsKey(c)) return false;
                cur = cur.childs.get(c);
            }
            return cur.type == 1;
        }

        public List<int[]> getStartIndex(String text) {
            List<int[]> ret = new ArrayList<>();
            Node cur = root;

            for (int i = 0; i < text.length(); ++i) {
                char c = text.charAt(i);
                while (!cur.isRoot() && !cur.childs.containsKey(c)) cur = cur.fail;
                if (cur.childs.containsKey(c)) cur = cur.childs.get(c);
                else continue;
                for (Node lastPattern = cur; lastPattern != root; lastPattern = lastPattern.lastPattern) {
                    if (lastPattern.type == 0) continue; // make sense of the cur node
                    ret.add(new int[]{lastPattern.patternId, i - lastPattern.depth + 1, i});
                }
            }
            return ret;
        }
    }


    //AC automation
    public static List<int[]> find(String text, List<String> patterns) {
        //if (patterns.size() == 1) return findSingle(text, patterns.get(0));
        ACAutomation acAM = new ACAutomation(patterns);
        List<int[]> tri = acAM.getStartIndex(text);
        List<int[]> res = new ArrayList<>();

        //find the ones without both children and parent
        Collections.sort(tri, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                if (o1[1] != o2[1]) return o1[1] - o2[1];
                return o2[2] - o1[2];
            }
        });
        for (int i = 0; i < tri.size(); ++i) {
            if (0 < i && res.get(res.size() - 1)[1] <= tri.get(i)[1] && tri.get(i)[2] <= res.get(res.size() - 1)[2]) continue;
            res.add(tri.get(i));
        }
        return res;
    }

    //AC automation
    public static List<int[]> find(String text, String[] patterns) {
        return find(text, new ArrayList<String>(Arrays.asList(patterns)));
    }


    //KMP TODO
    public static List<int[]> findSingle(String text, String pattern) {
       return null;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/bankNames.txt")));
        List<String> patterns = new ArrayList<>();
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            patterns.add(line);
        }

        patterns.add("交通银行");
        patterns.add("银行");
        patterns.add("交通");
        patterns.add("通");
        patterns.add("银行信用");

        String text = "动动手指，得5000积分！9月30日前用交通银行信用卡主卡注册客户端手机银行并完成首次登录可获赠5000积交通银行分，每位客户奖励仅限一次。手机访问wap.95559.com.cn/dl 下载\\u201c交通银行\\u201d客户端。注册流程见信用卡网站\\u201c电子银行\\u201d。[交通银行卡中心]​";
        List<int[]> startIndexes = Strings.find(text, patterns);
        for (int[] p: startIndexes) {
            System.out.println(patterns.get(p[0]) + ": [" + p[1] + ", " + p[2] + "]");
        }
    }
}
