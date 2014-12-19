package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.*;
import java.util.*;

/**
 * Created by root on 14-9-28.
 */
public class Emoji extends Rule {
    CombEmoji combEmoji;
    SingleEmoji singleEmoji;
    int singleEmojiN, combEmojiN;
    List<String> Emojis;
    private List<String> curEmojis;

    PrintWriter extractedRulesOut;

    class Range implements Comparable<Range> {
        int l, r;

        public Range(int l, int r) {
            this.l = l;
            this.r = r;
        }

        @Override
        public int compareTo(Range o) {
            if (l != o.l) return l - o.l;
            return r - o.r;
        }
    }
    public Emoji() {
        try {
            extractedRulesOut = new PrintWriter(new FileOutputStream("data/extractedEmojis.txt"));
            Emojis = new ArrayList<>();
            curEmojis = new ArrayList<>();
            singleEmoji = new SingleEmoji();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/composedEmojis.txt")));
            List<String> emojis = new ArrayList<>();
            while (true) {
                String emoji = in.readLine();
                if (emoji == null) break;
                emojis.add(emoji);
            }
            combEmoji = new CombEmoji(emojis);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        singleEmojiN = combEmojiN = 0;
    }

    @Override
    public void updRemainingBody(Corpus cps) {
        List<String> nsegs = new ArrayList<>();
        curEmojis.clear();
        for (String line : cps.getRemainingBody()) {
            Map<String, List<Integer>> ps = singleEmoji.getEmojis(line);
            List<Range> ranges = new ArrayList<>();
            for (String pattern : ps.keySet()) {
                curEmojis.add(pattern);
                for (int l : ps.get(pattern)) ranges.add(new Range(l, l + pattern.getBytes().length - 1));
            }
            singleEmojiN = ranges.size();
            byte[] bytes = line.getBytes();
            ranges = disjoin(ranges, bytes.length); //TODO
            StringBuffer sb = new StringBuffer("");
            for (int i = 0, l = 0; i < ranges.size() && l < bytes.length; l = ranges.get(i).r + 1, ++i) {
                sb.append(new String(bytes, l, ranges.get(i).l - l));
            }
            line = sb.toString();

            ranges = combEmoji.getEmojis(line);
            for (Range R: ranges) {
                curEmojis.add(line.substring(R.l, R.r + 1));
            }
            combEmojiN = ranges.size();
            if (combEmojiN == 0) {
                nsegs.add(line);
            }
            ranges = disjoin(ranges, line.length()); //TODO
            for (int i = 0, l = 0; i < ranges.size() && l < line.length(); l = ranges.get(i).r + 1, ++i) {
                nsegs.add(line.substring(l, ranges.get(i).l));
            }
        }
        cps.setRemainingBody(nsegs);
        Emojis.addAll(curEmojis);
    }

    @Override
    public String[] getSubFeatureNames() {
        return new String[]{"single-emoji", "combined-emoji"};
    }

    @Override
    public void train(List<Corpus> cpss) {
        for (Corpus cps: cpss)
            updRemainingBody(cps);
        //modelOut.write(Emojis);
    }

    @Override
    public void process(Corpus cps) {
        updRemainingBody(cps);
        extractedRulesOut.println(curEmojis);
        cps.getX()[this.getStartIndex() + 0] = singleEmojiN > 0 ? 1 : 0;
        cps.getX()[this.getStartIndex() + 1] = combEmojiN > 0 ? 1 : 0;
    }

    private List<Range> disjoin(List<Range> ranges, int N) {
        List<Range> disjoinedR = new ArrayList<>();
        Collections.sort(ranges);
        ranges.add(new Range(N, N));
        for (int i = 1, l = ranges.get(0).l, r = ranges.get(0).r; i < ranges.size(); ++i) {
            if (r < ranges.get(i).l) {
                disjoinedR.add(new Range(l, r));
                l = ranges.get(i).l;
                r = ranges.get(i).r;
                continue;
            }
            r = Math.max(ranges.get(i).r, r);
        }
        disjoinedR.add(new Range(N, N));
        return disjoinedR;
    }

    @Override
    public String toString() {
        return "Emoji";
    }

    @Override
    public void readDef(DataInputStream dataIn) throws IOException {

    }

    @Override
    public void writeDef(DataOutputStream dataOut) throws IOException {

    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        /*
        List<String> patterns = new ArrayList<>();
        patterns.add("hello");
        patterns.add("lo");
        patterns.add("ll");
        patterns.add("hel");

        CombEmoji acAuto = new CombEmoji(patterns);
        String text = "hello world";
        System.modelOut.println(acAuto.getEmojis(text));

        String t = "😀";
        byte[] bytes = t.getBytes("UTF-16");//default to UTF-8
        for (byte bt: bytes) System.modelOut.print(Integer.toHexString(Byte.toUnsignedInt(bt)) + ", ");
        System.modelOut.println();
        for (char c: t.toCharArray()) System.modelOut.print(Integer.toHexString(c) + ", ");
        System.modelOut.println();

        System.modelOut.println();
        System.modelOut.println(t);
        SingleEmoji filterNoOverlap = new SingleEmoji();
        System.modelOut.println(filterNoOverlap.getEmojis(t));
        System.modelOut.println((int)Byte.MAX_VALUE + ", " + Character.MIN_VALUE);
        */
        Emoji emojiRule = new Emoji();
        String t = "你x-D怎么了呀？😂😂😂😂😂😂😂😂😂😂也不说😂";
    }

    class SingleEmoji {
        //Map [emotion -> offset modelIn bytes[]]
        public Map<String, List<Integer>> getEmojis(String s) {
            Map<String, List<Integer>> emojis = new HashMap<>();
            byte[] bytes = s.getBytes();
            for (int i = 0; i < bytes.length; ) {
                int c = Byte.toUnsignedInt(bytes[i]);
                int ones = headOne(c);
                String emoji = new String(bytes, i, ones);
                if (3 < ones && isEmoticon(emoji)) {
                    if (!emojis.containsKey(emoji)) emojis.put(emoji, new ArrayList<>());
                    emojis.get(emoji).add(i);
                }
                i += Math.max(ones, 1);
            }
            return emojis;
        }

        private int headOne(int c) {
            int res = 0;
            for (int mask = 0x80; mask > 0; mask >>= 1) {
                if ((c & mask) == 0) break;
                ++res;
            }
            return res;
        }

        private long toLong(byte[] bs) {
            long bsum = 0;
            for (int i = bs.length - 1; i >= 0; --i)
                bsum += (1L << (bs.length - 1 - i) * 8) * Byte.toUnsignedInt(bs[i]);
            return bsum;
        }

        private boolean isCR(byte[] bs) {
            return toLong(bs) == 0x0000000D;
        }

        private boolean isLF(byte[] bs) {
            return toLong(bs) == 0x0000000A;
        }

        private boolean isEmoticon(String emoji) {
            byte[] bs = emoji.getBytes(); //TODO
            long[] l = {Integer.toUnsignedLong(0xF09F9880), 0x00E298B9};//0x0000263A
            long[] r = {Integer.toUnsignedLong(0xF09F9980), 0x00E298BB};//0x0000263B
            boolean res = false;
            for (int i = 0; i < l.length; ++i)
                res |= l[i] <= toLong(bs) && toLong(bs) <= r[i];
            return res;
        }
    }


    class CombEmoji {
        class Node {
            char c;
            int type;// 1: end, 0: else
            int depth;
            Map<Character, Node> childs;
            Node fa, fail, lastPattern;
            String pattern;

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

            public String word() {
                if (pattern != null) return pattern;
                StringBuffer sb = new StringBuffer();
                for (Node cur = this; !cur.isRoot(); cur = cur.fa) sb.append(cur.c);
                pattern = sb.reverse().toString();
                return pattern;
            }
        }

        Node root;

        public CombEmoji(List<String> ss) {
            root = new Node(0, '\0', root);
            for (String s : ss) insert(s);
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


        public void insert(String s) {
            Node cur = root;
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (!cur.childs.containsKey(c))
                    cur.childs.put(c, new Node(0, c, cur));
                cur = cur.childs.get(c);
            }
            cur.type = 1;
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

        public List<Range> getEmojis(String s) {
            List<Range> ret = new ArrayList<>();
            Node cur = root;

            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                while (!cur.isRoot() && !cur.childs.containsKey(c)) cur = cur.fail;
                if (cur.childs.containsKey(c)) cur = cur.childs.get(c);
                else continue;
                for (Node lastPattern = cur; lastPattern != root; lastPattern = lastPattern.lastPattern) {
                    if (lastPattern.type == 0) continue; // make sense of the cur node
                    ret.add(new Range(i - lastPattern.depth + 1, i));
                }
            }
            return ret;
        }

    }
}