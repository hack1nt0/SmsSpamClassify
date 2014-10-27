package com.xiaomi.smsspam.preprocess;

import com.xiaomi.smsspam.Utils.Corpus;

import java.io.*;
import java.util.*;

/**
 * Created by root on 14-9-28.
 */
public class Emoji extends RulePrevious {
    CombEmoji combEmoji;
    SingleEmoji singleEmoji;
    int singleEmojiN, combEmojiN;

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
    public boolean fit(Corpus cps, int startIndex) {
        if (singleEmojiN > 0) cps.getRulesPreHits()[startIndex + 0] = 1;
        if (combEmojiN > 0) cps.getRulesPreHits()[startIndex + 1] = 1;
        return singleEmojiN > 0 || combEmojiN > 0;
    }

    @Override
    public int subClassCount() {
        return 2;
    }

    @Override
    public void train(List<Corpus> cpss) {

    }

    @Override
    protected List<String> process(String s) {
        List<String> ret = new ArrayList<>();

        Map<String, List<Integer>> ps = singleEmoji.getEmojis(s);
        List<Range> ranges = new ArrayList<>();
        for (String pattern : ps.keySet())
            for (int l : ps.get(pattern)) ranges.add(new Range(l, l + pattern.getBytes().length - 1));
        singleEmojiN = ranges.size();
        byte[] bytes = s.getBytes();
        ranges = disjoin(ranges, bytes.length);
        StringBuffer sb = new StringBuffer("");
        for (int i = 0, l = 0; i < ranges.size() && l < bytes.length; l = ranges.get(i).r + 1, ++i) {
            sb.append(new String(bytes, l, ranges.get(i).l - l));
        }
        s = sb.toString();

        ranges = combEmoji.getEmojis(s);
        combEmojiN = ranges.size();
        if (combEmojiN == 0) {
            ret.add(s);
            return ret;
        }
        ranges = disjoin(ranges, s.length());
        for (int i = 0, l = 0; i < ranges.size() && l < s.length(); l = ranges.get(i).r + 1, ++i) {
            ret.add(s.substring(l, ranges.get(i).l));
        }
        return ret;
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
    public String getName() {
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
        System.out.println(acAuto.getEmojis(text));

        String t = "😀";
        byte[] bytes = t.getBytes("UTF-16");//default to UTF-8
        for (byte bt: bytes) System.out.print(Integer.toHexString(Byte.toUnsignedInt(bt)) + ", ");
        System.out.println();
        for (char c: t.toCharArray()) System.out.print(Integer.toHexString(c) + ", ");
        System.out.println();

        System.out.println();
        System.out.println(t);
        SingleEmoji filter = new SingleEmoji();
        System.out.println(filter.getEmojis(t));
        System.out.println((int)Byte.MAX_VALUE + ", " + Character.MIN_VALUE);
        */
        Emoji emojiRule = new Emoji();
        String t = "你x-D怎么了呀？😂😂😂😂😂😂😂😂😂😂也不说😂";
        System.out.println(emojiRule.process(t));
    }

    class SingleEmoji {
        //Map [emotion -> offset in bytes[]]
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