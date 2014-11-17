package com.xiaomi.smsspam.Utils;

import java.io.*;
import java.util.*;

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

    private void insert(String s, int index) {
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
        List<int[]> ret = new ArrayList<>();
        if (text.length() <= startIndex || startIndex < 0) {
            return ret;
        }

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

interface PatternMinable {
    //得到待挖掘的句子语料
    public String getCorpus();

    //得到句子语料的最小单元集合
    public List<String> getTokens();
}


class MiningPatterns {

    class Pattern implements Comparable<Pattern>{
        public void setPattern(List<String> pattern) {
            this.pattern = pattern;
        }

        List<String> pattern;
        Map<Integer,Map<String,List<Integer>>> wildcards;
        Set<Integer> sourceIndex;
        int lc, rc;
        int sizeInChar;

        public int getSourceId() {
            return sourceId;
        }

        int sourceId; //which line in corpus

        /*
        Information(String rawPattern) {
            this.rawPattern = rawPattern;
        }*/

        Pattern(List<String> pattern, Set<Integer> sourceIndex, int lc, int rc, int sourceId) {
            this.pattern = pattern;
            this.sourceIndex = sourceIndex;
            this.lc = lc; this.rc = rc;
            this.sourceId = sourceId;
            wildcards = new HashMap<>();
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
            StringBuffer prtStr = new StringBuffer(sourceIndex.size()  + "\t");
            for (int i = 0; i <= pattern.size(); ++i) {
                if (wildcards.containsKey(i) && wildcards.get(i).size() > 1) {
                    prtStr.append("<*:");
                    for (String candW: wildcards.get(i).keySet())
                        prtStr.append(candW).append("|");
                    prtStr.replace(prtStr.length() - 1, prtStr.length(), ">");
                    //prtStr.append(sepSymbol);
                }
                if (i < pattern.size()) prtStr.append(pattern.get(i));
            }
            prtStr.append("\t").append(sourceIndex.toString());
            for (Integer si: sourceIndex) {
                prtStr.append(lines.get(si));
                break;
            }
            return prtStr.toString();
        }

        public List<String> getPattern() {
            return pattern;
        }

        @Override
        public int compareTo(Pattern o) {
            if (o.sizeInChar != this.sizeInChar) return o.sizeInChar - this.sizeInChar;
            return o.getSup() - this.getSup();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Pattern)) {
                throw new RuntimeException("equals with non-valid type");
            }
            return this.sourceIndex.equals(((Pattern) obj).sourceIndex);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public void updWildcards(int p, String content, int sourceId) {
            if (!wildcards.containsKey(p)) wildcards.put(p, new HashMap<>());
            if (!wildcards.get(p).containsKey(content)) wildcards.get(p).put(content, new ArrayList<>());
            wildcards.get(p).get(content).add(sourceId);
        }
    }

    Pattern[] patterns;
    boolean[] invalid;
    List<String> lines = new ArrayList<>();
    Pattern[][] MCSubSeq;
    int minSup;
    int maxCorpusLen = 0;
    int[][] dp;
    String sepSymbol = "<*>";
    String[] tags = {"<RealNumber>" ,"<TimeSpan>", "<Flow>", "<Money>", "<BankCardNumber>", "<ExpressNumber>", "<PhoneNumber>", "<URL>", "<Time>", "<VerificationCode>​"};
    ACAutomation acAutomation = new ACAutomation(Arrays.asList(tags));
    double supRatio;
    ACAutomation filterDict;
    ACAutomation knowledgeDict;

    //构造函数
    public MiningPatterns(double supRatio, String filterDictPath) {
        this.supRatio = supRatio;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filterDictPath)));
            List<String> filterTokens = new ArrayList<>();
            List<String> knowledgeTokens = new ArrayList<>();
            for (int i = 0;; ++i) {
                String line = in.readLine();
                if (line == null) break;
                String[] tokens = line.split(" ");
                filterTokens.add(tokens[0]);
                if (tokens.length > 1) knowledgeTokens.add(tokens[0]);
            }
            filterDict = new ACAutomation(filterTokens);
            knowledgeDict = new ACAutomation(knowledgeTokens);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean inital(List<? extends PatternMinable> toBeMine) {

        for (int i = 0; i < toBeMine.size(); ++i) {
            List<String> tokens = toBeMine.get(i).getTokens();
            patterns[i] = new Pattern(tokens, new HashSet<>(Arrays.asList(i)), i, i, i);
            maxCorpusLen = Math.max(toBeMine.get(i).getCorpus().length(),
                    maxCorpusLen);
        }
        int lineN = toBeMine.size();
        patterns = new Pattern[lineN * 2];
        invalid = new boolean[lineN * 2];
        MCSubSeq = new Pattern[lineN * 2][lineN * 2];
        dp = new int[maxCorpusLen + 2][maxCorpusLen + 2];
        minSup = (int) Math.floor(lineN * supRatio);
        return true;
    }


    boolean initial(String fileName) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                lines.add(line);
                maxCorpusLen = Math.max(line.length(), maxCorpusLen);
            }
            int linesN = lines.size();
            patterns = new Pattern[linesN * 2];
            invalid = new boolean[linesN * 2];
            MCSubSeq = new Pattern[linesN * 2][linesN * 2];
            dp = new int[maxCorpusLen + 2][maxCorpusLen + 2];
            for (int i = 0; i < lines.size(); ++i) {
                List<String> tokens = getTokens(lines.get(i));
                patterns[i] = new Pattern(tokens, new HashSet<>(Arrays.asList(i)), i, i, i);
            }
            minSup = (int)Math.floor(linesN * supRatio);

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
                            if (invalid[i] || invalid[patternsN - 1]) continue;
                            MCSubSeq[patternsN - 1][i] = getLCSeq(patternsN - 1, i);
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

 //生成patterns
    List<Pattern> getPatWithPosition() {
        List<Pattern> ret = new ArrayList<>();
        PriorityQueue<Pattern> priQueue = new PriorityQueue<>();
        for (int cur = lines.size(); cur < lines.size() * 2 - 1; ++cur) {
            //System.out.println("[" + (nNode - linesN + 1) * 100.0 / (linesN - 1) + "%] finished.");
            int candI = 0, candJ = 0;
            for (int i = cur - 1; i >= 0; --i) {
                /*
                if (MCSubSeq[i][0] == null) {
                    getMCSubSeqMulT = new GetMCSubSeq(i + 1);
                    getMCSubSeqMulT.start();
                    while(getMCSubSeqMulT.isRunning());
                }*/
                for (int j = 0; j < i; ++j) {
                    //if (invalid[i] || invalid[j] || used[i] && used[j]) continue;
                    if (invalid[i] || invalid[j]) continue;
                    if (MCSubSeq[i][j] != null) continue;
                    MCSubSeq[i][j] = getValidLCSeq(i, j);
                    //MCSubSeq[i][j] = getLCSeq(i, j);
                    priQueue.add(MCSubSeq[i][j]);
                }
            }

            Pattern maxPattern = null;

            while (!priQueue.isEmpty()) {
                maxPattern = priQueue.poll();
                if (invalid[maxPattern.lc] || invalid[maxPattern.rc]) {
                    maxPattern = null;
                    continue;
                }
                break;
            }

            if (maxPattern == null) break;
            candI = maxPattern.lc; candJ = maxPattern.rc;

            //System.out.println(candI + " + " + candJ + " -> " + cur);
            patterns[cur] = maxPattern;
            invalid[candI] = invalid[candJ] = true;

            for (int i = 0; i < lines.size(); ++i) { // find all texts that consist of the commonP
                //if (i == candI || i == candJ) continue;
                //if (patterns[cur].sourceIndex.contains(i)) continue;
                if (isSubStr(patterns[cur], patterns[i])) {
                    patterns[cur].sourceIndex.add(i);
                }
            }
            if (patterns[cur].getSup() < minSup) continue;

            //filter overlay ones
            boolean repated = false;
            for (int i = 0; i < ret.size(); ++i) {
                if (isSubStr(patterns[cur], ret.get(i))) {
                    //ret.remove(ret.get(i));
                    repated = true;
                    break;
                }
                //if (patterns[cur].equals(ret.get(i))) { repated = true; break;}
            }
            if (!repated) {
                ret.add(patterns[cur]);
                System.out.println(ret.get(ret.size() - 1));
            }
            invalid[cur] = true;
        }
        return ret;
    }

    //check and updWildcards
    private boolean isSubStr(Pattern A, Pattern B) {
        if (A.size() > B.size()) return false;
        if (B.getSourceId() < 0) {
            for (int i = 0, j = 0; i < A.size();) {
                while (j < B.size() && !B.get(j).equals(A.get(i))) ++j;
                if (B.size() <= j) {
                    return false;
                }
                ++i; ++j;
            }
            return true;
        }

        int N = A.size() + 2, M = B.size() + 2;//extended the head and the tail
        boolean[][] dp = new boolean[N + 1][M + 1];
        dp[N][M] = dp[N - 1][M - 1] = true;
        int[][] trail = new int[N][M];

        for (int i = N - 2; i >= 0; --i) //A index
            for (int j = M - 2; j >= 0; --j) {// B index
                int ri = i - 1, rj = j - 1;
                //if (realI < 0 && 0 < realJ || realJ < 0 && 0 < realI || 0 < realI && 0 < realJ && !A.get(realI).equals(B.get(realJ))) continue;
                if (!(i == 0 && j == 0 || ri >= 0 && rj >= 0 && A.get(ri).equals(B.get(rj)))) continue;

                boolean res = false;
                StringBuffer sb = new StringBuffer("");
                while (++rj < B.size()) {//&& (i == N - 2 || curJ + N - i < B.size())) {
                    sb.append(B.get(rj));
                    if (!isValidWildcard(A, i, i + 1, sb.toString())) break;
                    if (ri + 1 < A.size() && B.get(rj).equals(A.get(ri + 1)) && dp[i + 1][rj + 1]) {
                        res = true;
                        trail[i][j] = rj;
                        break;
                    }
                }
                if (i == N - 2 && B.size() <= rj) {
                    res = true;
                    trail[i][j] = rj;
                }
                dp[i][j] = res;
            }
        /*
        boolean ret = false;
        for (int i = 0; i <= M; ++i) ret |= dp[0][i];
        if (!ret) return false;*/
        if (!dp[0][0]) return false;

        for (int i = 0, realJ = -1; i < N - 1; ++i) {
            StringBuilder stringBuffer = new StringBuilder("");
            for (int tmpJ = realJ + 1; tmpJ < B.size() && tmpJ < trail[i][realJ + 1]; ++tmpJ) stringBuffer.append(B.get(tmpJ));
            A.updWildcards(i, stringBuffer.toString(), B.getSourceId());
            realJ = trail[i][realJ + 1];
        }
        return true;
    }

    private Pattern getValidLCSeq(int ai, int bi) {
        Pattern cand = getLCSeq(ai, bi);
        if (cand.size() <= 0) return cand;
        Pattern A = patterns[ai], B = patterns[bi];

        int N = cand.size() + 2, M1 = A.size() + 2, M2 = B.size() + 2;//extended the head and the tail

        int[][] dp1 = new int[N + 1][M1 + 1];
        int[] len1 = new int[cand.size()];
        getMaxLen(cand, A, dp1, len1);

        int[][] dp2 = new int[N + 1][M2 + 1];
        int[] len2 = new int[cand.size()];
        getMaxLen(cand, B, dp2, len2);

        for (int i = 0; i < len1.length; ++i) len1[i] = Math.min(len1[i], len2[i]);
        int candi = 0;
        for (int i = 0; i < len1.length; ++i) if (len1[i] > len1[candi]) candi = i;

        List<String> nPatternList = new ArrayList<>();
        for (int i = candi; i < candi + len1[candi]; ++i) {
            if (i >= cand.size()) throw new RuntimeException(candi + ", " + len1[candi] + ", " + cand.size());
            nPatternList.add(cand.get(i));
        }
        cand.setPattern(nPatternList);
        return cand;
    }

    private void getMaxLen(Pattern cand, Pattern A, int[][] dp, int[] len) {
        int N = cand.size() + 2, M = A.size() + 2;
        dp[N][M] = dp[N - 1][M - 1] = 1;
        for (int i = N - 2; i >= 1; --i)
            for (int j = M - 2; j >= 1; --j) {
                int ri = i - 1, rj = j - 1;
                //if (realI < 0 && 0 < realJ || realJ < 0 && 0 < realI || 0 < realI && 0 < realJ && !A.get(realI).equals(B.get(realJ))) continue;
                if (!(i == 0 && j == 0 || ri >= 0 && rj >= 0 && cand.get(ri).equals(A.get(rj)))) continue;

                int res = 1;
                StringBuffer sb = new StringBuffer("");
                boolean find = false;
                for (int k = rj + 1; k < A.size(); ++k) {
                    sb.append(A.get(k));
                    if (!isValidWildcard(A, i, i + 1, sb.toString())) break;
                    if (i + 1 < cand.size() && A.get(k).equals(cand.get(i + 1)) && dp[i + 1][k] > 0) {
                        find = true;
                        res = Math.max(dp[i + 1][k] + 1, res);
                    }
                }
                if (!find) {
                    sb.setLength(0);
                    for (int k = rj + 1; k < A.size(); ++k) sb.append(A.get(k));
                    if (!isValidWildcard(A, i, A.size() + 1, sb.toString())) res = -1;
                }
                dp[i][j] = res;
            }

        StringBuffer prefix = new StringBuffer("");
        int ret = 0;
        for (int i = 0, j = 0; i < cand.size() && j < A.size(); ++j) {
            if (cand.get(i).equals(A.get(j))) {
                int res = isValidWildcard(A, 0, j + 1, prefix.toString()) ? dp[i + 1][j + 1] + 1 : -1;
                ret = Math.max(res, ret);
                ++i; continue;
            }
            prefix.append(A.get(j));
        }
        dp[0][0] = ret;

        for (int i = 0; i < cand.size(); ++i)
            for (int j = 0; j < A.size(); ++j) len[i] = Math.max(dp[i + 1][j + 1], len[i]);
    }


    //token is valid in the context of (A[L], A[R])
    private boolean isValidWildcard(Pattern A, int L, int R, String token) {
        //(L, [A], R), extended A's pattern[]
        if (L == 0 || R == A.size() + 1) return knowledgeDict.match(token, 0).size() == 0;
        return filterDict.match(token, 0).size() == 0;
    }

    private Pattern getLCSeq(int ai, int bi) {
        Pattern A = patterns[ai], B = patterns[bi];
        int N = A.size(), M = B.size();
        for (int i = 1; i <= N; ++i) {
            for (int j = 1; j <= M; ++j) {
                int res = 0;
                if (A.get(i - 1).equals(B.get(j - 1))) res = Math.max(dp[i - 1][j - 1] + 1, res);
                else res = Math.max(dp[i - 1][j], dp[i][j - 1]);
                dp[i][j] = res;
            }
        }
        //Set<Integer> curSI = new HashSet<>(A.sourceIndex);
        //curSI.addAll(B.sourceIndex);
        Pattern ret = new Pattern(new ArrayList<>(), new HashSet<>(), ai, bi, -1);

        for (int i = N, j = M; 0 < i && 0 < j; ) {
            if (A.get(i - 1).equals(B.get(j - 1))) {
                ret.add(A.get(i - 1));
                --i;
                --j;
                continue;
            }
            if (dp[i][j] == dp[i - 1][j]) --i;
            else --j;
        }
        Collections.reverse(ret.getPattern());
        return ret;
    }


    public List<Pattern> expand(List<Pattern> seeds) {
        StringBuffer tmp = new StringBuffer("");
        for (Pattern seed: seeds) {
            //Set<Integer> nSourceIndex = new HashSet<>();
            for (int si: seed.sourceIndex) {
                Pattern source = patterns[si];
                for (int i = 0, j = 0; i < seed.size();) {
                    tmp.setLength(0);
                    while (!seed.get(i).equals(source.get(j)))
                        tmp.append(source.get(j++));
                    //putSthInNestMap(seed.wildcards, i, tmp.toString(), si);
                    seed.updWildcards(i, tmp.toString(), si);
                    ++i;
                    ++j;
                    if (i == seed.size()) {
                        tmp.setLength(0);
                        while (j < source.size()) tmp.append(source.get(j++));
                        //putSthInNestMap(seed.wildcards, i, tmp.toString(), si);
                        seed.updWildcards(i, tmp.toString(), si);
                    }
                }
            }
        }
        return seeds;
    }

    public static void main(String[] args) throws IOException {

        String corpusFilePath = "data/NLP/bug.txt";
        String filterDictPath = "data/NLP/filterDict.txt";
        MiningPatterns miningPatterns = new MiningPatterns(0.6, filterDictPath);
        miningPatterns.initial(corpusFilePath);

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(corpusFilePath + ".ext")));

        long start = System.currentTimeMillis();
        List<Pattern> ps = miningPatterns.getPatWithPosition();
        for (Pattern p: ps) {
            out.write(p.toString());
            out.write("\n");
        }
        long stop = System.currentTimeMillis();

        out.close();
        System.out.println("Time Consumed: " + (stop - start) / 1000.0 + "s");
    }

}
