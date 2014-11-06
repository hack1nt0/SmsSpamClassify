package com.xiaomi.smsspam.Utils;

import TC.PaperAndPaintEasy;
import sun.nio.ch.ThreadPool;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by dy on 14-10-25.
 */
public class Strings {


    public static void main(String[] args) throws IOException {
        List<String> patterns = new ArrayList<>();
        patterns.add("bc");
        patterns.add("b");
        patterns.add("def");
        patterns.add("e");
        patterns.add("ef");
        String text = "abcdef";

        ACAutomation acAutomation = new ACAutomation(patterns);
        List<int[]> startIndexes = acAutomation.find(text);
        for (int[] p: startIndexes) {
            System.out.println(patterns.get(p[0]) + ": [" + p[1] + ", " + p[2] + "]");
        }
        System.out.println();

        startIndexes = acAutomation.find(text, 4);
        for (int[] p: startIndexes) {
            System.out.println(patterns.get(p[0]) + ": [" + p[1] + ", " + p[2] + "]");
        }
        System.out.println();

        startIndexes = acAutomation.findFirstAll(text, 0);
        for (int[] p: startIndexes) {
            System.out.println(patterns.get(p[0]) + ": [" + p[1] + ", " + p[2] + "]");
        }
        System.out.println();

        startIndexes = new ArrayList<int[]>(){{add(acAutomation.findFirst(text, 0));}};
        for (int[] p: startIndexes) {
            System.out.println(patterns.get(p[0]) + ": [" + p[1] + ", " + p[2] + "]");
        }
        System.out.println();

        System.out.println(acAutomation.contains(patterns.get(0)));
        System.out.println(acAutomation.contains(patterns.get(0) + "23"));
    }
}

class ACAutomation {

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

    public List<int[]> match(String text, int startIndex) {
        if (text.length() <= startIndex || startIndex < 0) {
            return null;
        }

        List<int[]> ret = new ArrayList<>();
        Node cur = root;

        for (int i = startIndex; i < text.length(); ++i) {
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

    //to left only the ones without both children and parent
    public List<int[]> filterNoOverlay(List<int[]> tri) {
        if (tri.size() == 0) return null;
        //if (patterns.size() == 1) return findSingle(text, patterns.get(0));
        List<int[]> res = new ArrayList<>();
        Collections.sort(tri, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                if (o1[1] != o2[1]) return o1[1] - o2[1];
                return o2[2] - o1[2];
            }
        });
        for (int i = 0; i < tri.size(); ++i) {
            if (0 < res.size() && tri.get(i)[1] <= res.get(res.size() - 1)[2])
                continue;
            res.add(tri.get(i));
        }
        return res;
    }

    //to left the left-most ones
    public List<int[]> filterLeftMost(List<int[]> tri) {
        if (tri.size() == 0) return null;
        //if (patterns.size() == 1) return findSingle(text, patterns.get(0));
        List<int[]> res = new ArrayList<>();

        for (int i = 0, LM = Integer.MAX_VALUE; i < tri.size(); ++i) {
            int curL = tri.get(i)[1];
            if (LM < curL) continue;
            if (curL < LM) res.clear();
            LM = Math.min(curL, LM);
            res.add(tri.get(i));
        }
        return res;
    }

    public int[] filterLongest(List<int[]> tri) {
        if (tri.size() == 0) return null;
        int longestI = 0;
        for (int i = 0; i < tri.size(); ++i) {
            int curL = tri.get(i)[2] - tri.get(i)[1] + 1;
            int maxL = tri.get(longestI)[2] - tri.get(longestI)[1] + 1;
            if (curL <= maxL) continue;
            curL = maxL;
            longestI = i;
        }
        return tri.get(longestI);
    }

    public List<int[]> find(String text) {
        return filterNoOverlay(match(text, 0));
    }

    //跟原先的功能一样，但是是从index位置开始找
    List<int[]> find(String text, int startIndex) {
        return filterNoOverlay(match(text, startIndex));
    }


    //返回最早出现在词典中的词（最长。例如Dic={"ab","bc","bcd","cde"}；​findFirst("abcdefg",1)​返回{"bcd"}）
    int[] findFirst(String text, int startIndex) {
        return filterLongest(findFirstAll(text, startIndex));
    }


    //返回最早出现在词典中的词（所有。例如Dic={"ab","bc","bcd","cde"}；findFirstAll("abcdefg",1)返回{"bc","bcd"}）
    List<int[]> findFirstAll(String text, int startIndex) {
        return filterLeftMost(match(text, startIndex));
    }


    //判断word是否是词典中的词（例如Dic={"ab","bc","bcd","cde"}；contain("abcdefg")返回false；contain("cde"​)返回true）
    int contains(String word) {
        Node cur = root;
        for (int i = 0; i < word.length(); ++i) {
            char c = word.charAt(i);
            if (!cur.childs.containsKey(c)) return -1;
            cur = cur.childs.get(c);
        }
        return cur.type == 1 ? cur.patternId : -1;
    }
}
class MiningPatterns {
    class Pattern implements Comparable<Pattern>{
        List<String> pattern;
        String rawPattern;
        Map<Integer,Map<String,List<Integer>>> patStar;
        Set<Integer> sourceIndex;
        int lc, rc;
        int sizeInChar;

        /*
        Information(String rawPattern) {
            this.rawPattern = rawPattern;
        }*/

        Pattern(List<String> pattern, Set<Integer> sourceIndex, int lc , int rc) {
            this.pattern = pattern;
            this.sourceIndex = sourceIndex;
            this.lc = lc;
            this.rc = rc;
        }

        public String get(int i) {
            return pattern.get(i);
        }

        public int size() {
            return pattern.size();
        }

        public int getSizeInChar() {
            return sizeInChar;
        }

        public void add(String nToken) {
            pattern.add(nToken);
            sizeInChar += nToken.equals(sepSymbol) ? 1 : nToken.length();
        }

        public int getSup() {
            return sourceIndex.size();
        }

        @Override
        public String toString() {
            return sourceIndex.size() + "\t" + pattern.toString() + "\t\t" + sourceIndex.toString();
        }

        public List<String> getPattern() {
            return pattern;
        }

        @Override
        public int compareTo(Pattern o) {
            return o.sizeInChar - this.sizeInChar;
        }
    }

    Pattern[] patterns;
    int[] sups0, sups1;
    boolean[] used;
    boolean[] invalid;
    static int N;
    double[][] threshold;
    Pattern[][] MCSubSeq;
    double minThreshold = 0.6;
    int minSup;
    int maxCorpusLen = 0;
    int[][] dp;
    //char[] sb;
    String sepSymbol = "<*>";
    String[] tags = {"<RealNumber>" ,"<TimeSpan>", "<Flow>", "<Money>", "<BankCardNumber>", "<ExpressNumber>", "<PhoneNumber>", "<URL>", "<Time>", "<VerificationCode>​"};
    ACAutomation acAutomation = new ACAutomation(Arrays.asList(tags));
    double supRatio;
    //构造函数
    public MiningPatterns(double supRatio) {
        this.supRatio = supRatio;
    }

    boolean initial(String fileName) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            List<String> lines = new ArrayList<>();
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                lines.add(line);
                maxCorpusLen = Math.max(line.length(), maxCorpusLen);
            }
            N = lines.size();
            patterns = new Pattern[N * 2];
            sups0 = new int[N * 2]; Arrays.fill(sups0, 1);
            sups1 = new int[N * 2];
            //used = new boolean[N * 2];
            invalid = new boolean[N * 2];
            used = new boolean[N * 2];
            threshold = new double[N * 2][N * 2];
            MCSubSeq = new Pattern[N * 2][N * 2];
            dp = new int[maxCorpusLen + 1][maxCorpusLen + 1];
            for (int i = 0; i < threshold.length; ++i) Arrays.fill(threshold[i], -1);
            for (int i = 0; i < lines.size(); ++i) {
                List<String> tokens = getTokens(lines.get(i));
                patterns[i] = new Pattern(tokens, new HashSet<>(Arrays.asList(i)), i, i);
            }
            minSup = (int)Math.floor(N * supRatio);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private List<String> getTokens(String text) {
        List<int[]> tagRanges = acAutomation.filterNoOverlay(acAutomation.match(text, 0));
        List<String> res = new ArrayList<>();
        for (int i = 0, j = 0; i < text.length();) {
            if (tagRanges != null && j < tagRanges.size() && tagRanges.get(j)[1] <= i && i <= tagRanges.get(j)[2]) {
                res.add(tags[tagRanges.get(j)[0]]);
                i = tagRanges.get(j)[2] + 1; ++j;
                continue;
            }
            res.add(text.charAt(i) + "");
            ++i;
        }
        return res;
    }

    class GetMCSubSeq {
        int cpuN = 1;
        Thread[] threads = new Thread[cpuN];
        volatile int allDone = cpuN; //TODO volatile

        final int[] from = new int[cpuN]; //TODO final
        final int[] to = new int[cpuN];

        public void start() {
            if (cpuN == 1) {threads[0].run(); return;}
            for (int i = 0; i < threads.length; ++i) threads[i].start();
        }

        GetMCSubSeq(int patternsN) {
            for (int i = 0; i < cpuN; ++i) {
                if (i == 0) {
                    from[i] = 0; to[i] = patternsN / cpuN; continue;
                }
                from[i] = to[i - 1] + 1;
                to[i] = Math.min(from[i] + patternsN / cpuN, patternsN);
            }
            allDone = cpuN;
            for (int i = 0; i < cpuN; ++i) {
                final int cur = i;
                threads[cur] = new Thread() {
                    @Override
                    public void run() {
                        //super.run();
                        for (int i = from[cur]; i < to[cur]; ++i) {
                            if (invalid[i] || invalid[patternsN - 1] || used[i] && used[patternsN - 1]) continue;
                            MCSubSeq[patternsN - 1][i] = getMCSubSeq(patternsN - 1, i);
                        }
                        --allDone;
                    }
                };
            }
        }

        public boolean isRunning() {
            //threads[0].isAlive(); //TODO
            return 0 < allDone;
        }
    }

    GetMCSubSeq getMCSubSeqMulT;

 //生成patterns
    List<Pattern> getPatWithPosition() {
        List<Pattern> ret = new ArrayList<>();
        PriorityQueue<Pattern> priQueue = new PriorityQueue<>();
        for (int cur = N; cur < N * 2 - 1; ++cur) {
            //System.out.println("[" + (nNode - N + 1) * 100.0 / (N - 1) + "%] finished.");
            int candI = 0, candJ = 0;
            for (int i = cur - 1; i >= 0; --i) {
                /*
                if (MCSubSeq[i][0] == null) {
                    getMCSubSeqMulT = new GetMCSubSeq(i + 1);
                    getMCSubSeqMulT.start();
                    while(getMCSubSeqMulT.isRunning());
                }*/
                for (int j = 0; j < i; ++j) {
                    if (invalid[i] || invalid[j] || used[i] && used[j]) continue;
                    if (MCSubSeq[i][j] != null) continue;

                    MCSubSeq[i][j] = getMCSubSeq(i, j);
                    priQueue.add(MCSubSeq[i][j]);
                }
            }

            Pattern maxPattern = null;

            while (!priQueue.isEmpty()) {
                maxPattern = priQueue.poll(); //TODO ??
                //System.out.println("MCSubSeq(" + i + ", " + j + "): " + MCSubSeq[i][j]);
                //if (MCSubSeq[i][j] == null) continue;
                    /*
                    threshold[i][j] = MCSubSeq[i][j].sizeInChar();// / (Math.min(patterns[i].length(), patterns[j].length()) + 0.0);
                    if (threshold[i][j] < threshold[candI][candJ]
                            || threshold[i][j] == threshold[candI][candJ] && patterns[i].getSup() + patterns[j].getSup() <= patterns[candI].getSup() + patterns[candJ].getSup())
                        continue; //TODO add used[i] && used[j] or not
                    candI = i;
                    candJ = j;
                    */
                if (invalid[maxPattern.lc] || invalid[maxPattern.rc] || used[maxPattern.lc] && used[maxPattern.rc]) {
                    maxPattern = null;
                    continue;
                }
                break;
            }

            if (maxPattern == null) break;
            candI = maxPattern.lc; candJ = maxPattern.rc;
            //assert candI == candJ;
            //if (candI == candJ) break;

            //System.out.println(candI + " + " + candJ + " -> " + cur);
            patterns[cur] = maxPattern;
            invalid[candI] = invalid[candJ] = true;
            //gc
            patterns[candI] = patterns[candJ] = null;
            for (int i = 0; i < cur - 1; ++i)
                MCSubSeq[i][candI] = MCSubSeq[candI][i] = MCSubSeq[i][candJ] = MCSubSeq[candJ][i] = null;
            //end of gc

            for (int i = 0; i < cur; ++i) { // find all texts that consist of the commonP
                if (invalid[i] || i == candI || i == candJ) continue;
                if (isSubStr(patterns[cur], patterns[i])) {
                    //sups1[cur] += sups0[i];
                    patterns[cur].sourceIndex.addAll(patterns[i].sourceIndex);
                    used[i] = true;
                }
            }
            if (patterns[cur].getSup() < minSup) continue;

            //filter overlay ones
            boolean repated = false;
            for (int i = 0; i < ret.size(); ++i) {
                if (isSubStr(patterns[cur], ret.get(i)) || patterns[cur].sourceIndex.equals(ret.get(i))) {repated = true; break;}
            }
            if (!repated) {
                ret.add(patterns[cur]);
                System.out.println(ret.get(ret.size() - 1));
            }
            //gc
            invalid[cur] = true;
            patterns[cur] = null;
            for (int i = 0; i < cur - 1; ++i) MCSubSeq[i][cur] = MCSubSeq[cur][i] = null;
            //if (cur % 4 == 0) System.gc(); //TODO
        }
        return ret;
    }

    private boolean isSubStr(Pattern A, Pattern B) {
        if (A.size() > B.size()) return false;
        for (int i = 0, j = 0; i < A.size();) {
            if (A.get(i).equals(sepSymbol)) {
                ++i; continue;
            }
            while (j < B.size() && !B.get(j).equals(A.get(i))) ++j;
            if (B.size() <= j) return false;
            ++i; ++j;
        }
        return true;
    }

    private Pattern getMCSubSeq(int ai, int bi) {
        Pattern A = patterns[ai], B = patterns[bi];
        int N = A.size(), M = B.size();
        //int[][] dp = new int[N + 1][M + 1];//TODO global sync?
        //int[][] cnt = new int[N + 1][M + 1];
        for (int i = 1; i <= N; ++i) {
            for (int j = 1; j <= M; ++j) {
                int res = 0;
                if (A.get(i - 1).equals(B.get(j - 1))) res = Math.max(dp[i - 1][j - 1] + 1, res);
                else res = Math.max(dp[i - 1][j], dp[i][j - 1]);
                dp[i][j] = res;
            }
        }
        Set<Integer> curSI = new HashSet<>(A.sourceIndex);
        curSI.addAll(B.sourceIndex);
        Pattern ret = new Pattern(new ArrayList<>(), curSI, ai, bi);

        for (int i = N, j = M; 0 < i && 0 < j; ) {
            if (A.get(i - 1).equals(B.get(j - 1))) { //TODO wrong to depending on cnt[i][j]==cnt[i-1][j-1]+1
                if (ret.size() == 0 || !ret.get(ret.size() - 1).equals(sepSymbol) || !A.get(i - 1).equals(sepSymbol))
                    ret.add(A.get(i - 1));
                --i;
                --j;
                continue;
            }
            if (dp[i][j] == dp[i - 1][j]) --i;
            else --j;
            if (0 < ret.size() && ret.get(ret.size() - 1).equals(sepSymbol)) continue;
            ret.add(sepSymbol);
        }
        Collections.reverse(ret.getPattern());
        return ret;
    }

    public static void main(String[] args) throws IOException {

        String filePath = "data/NLP/10086.txt";
        MiningPatterns miningPatterns = new MiningPatterns(0.001);
        miningPatterns.initial(filePath);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + ".ext")));

        long start = System.currentTimeMillis();
        List<Pattern> ps = miningPatterns.getPatWithPosition();
        for (Pattern p: ps) out.write(p.toString());
        out.close();
        long stop = System.currentTimeMillis();
        System.out.println("Time Consumed: " + (stop - start) / 1000.0 + "s");
    }

}
