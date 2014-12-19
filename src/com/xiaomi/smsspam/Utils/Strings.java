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

    public List<int[]> startWith(String text, int startIndex) {
        Node cur = root;
        List<int[]> ret = new ArrayList<>();
        for (int i = startIndex; i < text.length(); ++i) {
            if (!cur.childs.containsKey(text.charAt(i))) break;
            cur = cur.childs.get(text.charAt(i));
            if (cur.type == 1) ret.add(new int[]{cur.patternId, startIndex, startIndex + i});
        }
        return ret;
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
        //if (patterns.size() == 1) return findSingle(text, patterns.get(0));
        List<int[]> res = new ArrayList<>();
        if (tri.size() == 0) return res;

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
        //if (patterns.size() == 1) return findSingle(text, patterns.get(0));
        List<int[]> res = new ArrayList<>();
        if (tri.size() == 0) return res;

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
    List<int[]> findFirstAll(String text, int startIndex) {//TODO
        List<int[]> ret = filterLeftMost(match(text, startIndex));
        Collections.sort(ret, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o2[2] - o1[2];
            }
        });
        return ret;
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
    class CorpusM implements PatternMinable {
        public String corpus;
        public String[] tags = {"<RealNumber>" ,"<TimeSpan>", "<Flow>", "<Money>", "<BankCardNumber>", "<ExpressNumber>", "<PhoneNumber>", "<URL>", "<Time>", "<VerificationCode>​"};
        public ACAutomation tagDict = new ACAutomation(Arrays.asList(tags));

        public CorpusM(String corpus) {
            this.corpus = corpus;
        }

        @Override
        public String getCorpus() {
            return corpus;
        }

        @Override
        public List<String> getTokens() {
            List<int[]> tagRanges = tagDict.filterNoOverlay(tagDict.match(corpus, 0));
            List<String> res = new ArrayList<>();
            for (int i = 0, j = 0; i < corpus.length();) {
                if (tagRanges != null && j < tagRanges.size() && tagRanges.get(j)[1] <= i && i <= tagRanges.get(j)[2]) {
                    res.add(tags[tagRanges.get(j)[0]]);
                    i = tagRanges.get(j)[2] + 1; ++j;
                    continue;
                }
                res.add(corpus.charAt(i) + "");
                ++i;
            }
            return res;
        }
    }

    class Pattern implements Comparable<Pattern>{
        public void setPattern(List<String> pattern) {
            this.pattern = pattern;
        }

        List<String> pattern; // [segment]
        Map<Integer,Map<String,List<Integer>>> wildcards; // [pos, [content, sourceId]]
        Set<Integer> sourceIndex; // all corpus containing this pattern, whose size indicates the pattern's support rating
        int lc, rc; // left and right child
        int sizeInChar;
        int sourceId; // which corpus (only making sense when the pattern is a corpus from the beginning)
        public String sepSymbol = "<*>"; //Todo useful?

        Pattern(List<String> pattern, Set<Integer> sourceIndex, int lc, int rc, int sourceId) {
            this.pattern = pattern;
            this.sourceIndex = sourceIndex;
            this.lc = lc; this.rc = rc;
            this.sourceId = sourceId;
            wildcards = new HashMap<>();
        }

        public int getSourceId() {
            return sourceId;
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
            StringBuffer sb = new StringBuffer(sourceIndex.size()  + "\t");
            for (int i = 0; i <= pattern.size(); ++i) {
                if (wildcards.containsKey(i) && wildcards.get(i).size() > 1) {
                    sb.append("<*:");
                    for (String candW: wildcards.get(i).keySet())
                        sb.append(candW).append("|");
                    sb.replace(sb.length() - 1, sb.length(), ">");
                    //prtStr.append(sepSymbol);
                }
                if (i < pattern.size()) sb.append(pattern.get(i));
            }
            sb.append("\t").append(sourceIndex.toString()).append("\t");
            for (Integer si: sourceIndex) {
                CorpusM corpusM = (CorpusM) toBeMine.get(si);
                sb.append(corpusM.getCorpus());
                break;
            }
            return sb.toString();
        }

        public List<String> getPattern() {
            return pattern;
        }

        @Override
        public int compareTo(Pattern o) {
            int la = (int)Math.sqrt(sizeInChar) * getSup();
            int lb = (int)Math.sqrt(o.sizeInChar) * o.getSup();
            return lb - la;
            /*
            if (o.sizeInChar != this.sizeInChar) return o.sizeInChar - this.sizeInChar;
            return o.getSup() - this.getSup();
            */
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

        public void updateWildcards(int p, String content, int sourceId) {
            if (!wildcards.containsKey(p)) wildcards.put(p, new HashMap<>());
            if (!wildcards.get(p).containsKey(content)) wildcards.get(p).put(content, new ArrayList<>());
            wildcards.get(p).get(content).add(sourceId);
        }
    }

    public Pattern[] patterns;
    public boolean[] invalid;
    //public List<String> lines;
    List toBeMine;
    public int lineN;
    public Pattern[][] lcp; //Longest Common Substring
    public int minSup;
    public int maxCorpusLen;
    public int[][] dp; //pre-allocate DP array
    public int[][] path; // tracker array in lcp
    public int[] lenA; // max extendence of each element of pattern A
    public int[] lenB; // max extendence of each element of pattern B


    public double supRatio;
    public ACAutomation invalidWildcardDict;
    public ACAutomation invalidBoundWildcardDict;

    //构造函数
    public MiningPatterns(double supRatio, String filterDictPath) {
        this.supRatio = supRatio;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filterDictPath)));
            List<String> list1 = new ArrayList<>();
            List<String> list2 = new ArrayList<>();
            for (int i = 0;; ++i) {
                String line = in.readLine();
                if (line == null) break;
                String[] tokens = line.split(" ");
                if (tokens.length > 1) list2.add(tokens[0]);
                list1.add(tokens[0]);
            }
            invalidWildcardDict = new ACAutomation(list1);
            invalidBoundWildcardDict = new ACAutomation(list2);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    boolean inital(List<? extends PatternMinable> toBeMine) {
        this.toBeMine = toBeMine;
        lineN = toBeMine.size();
        patterns = new Pattern[lineN * 2];
        for (int i = 0; i < toBeMine.size(); ++i) {
            List<String> tokens = toBeMine.get(i).getTokens();
            patterns[i] = new Pattern(tokens, new HashSet<>(Arrays.asList(i)), i, i, i);
            maxCorpusLen = Math.max(toBeMine.get(i).getCorpus().length(),
                    maxCorpusLen);
        }
        invalid = new boolean[lineN * 2];
        lcp = new Pattern[lineN * 2][lineN * 2];
        dp = new int[maxCorpusLen + 2][maxCorpusLen + 2];
        path = new int[maxCorpusLen + 2][maxCorpusLen + 2];
        lenA = new int[maxCorpusLen + 2];
        lenB = new int[maxCorpusLen + 2];
        minSup = (int) Math.floor(lineN * supRatio);
        return true;
    }

    boolean initial(Reader reader) {
        try {
            BufferedReader in = new BufferedReader(reader);
            toBeMine = new ArrayList<>();
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                toBeMine.add(new CorpusM(line.trim()));
                maxCorpusLen = Math.max(line.length(), maxCorpusLen);
            }
            int linesN = toBeMine.size();
            patterns = new Pattern[linesN * 2];
            invalid = new boolean[linesN * 2];
            lcp = new Pattern[linesN * 2][linesN * 2];
            dp = new int[maxCorpusLen + 2][maxCorpusLen + 2];
            path = new int[maxCorpusLen + 2][maxCorpusLen + 2];
            lenA = new int[maxCorpusLen + 2];
            lenB = new int[maxCorpusLen + 2];

            for (int i = 0; i < toBeMine.size(); ++i) {
                CorpusM corpusM = (CorpusM)toBeMine.get(i);
                List<String> tokens = corpusM.getTokens();
                patterns[i] = new Pattern(tokens, new HashSet<>(Arrays.asList(i)), i, i, i);
                //System.out.println(patterns[i].toString());
            }
            minSup = (int)Math.floor(linesN * supRatio);

        } catch (IOException e) {
            e.printStackTrace();
        }
        lineN = toBeMine.size();
        return true;
    }



    class GetMCSubSeq { //an useless thread
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
                            lcp[patternsN - 1][i] = getLcs(patternsN - 1, i);
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
        if (lineN == 0) return ret;
        if (lineN == 1) {
            ret.add(getLcp(0, 0));
            return ret;
        }
        PriorityQueue<Pattern> priQueue = new PriorityQueue<>();
        for (int i = 0; i < lineN; ++i) { // get lcp of i and j
            for (int j = i + 1; j < lineN; ++j) {
                lcp[i][j] = getLcp(i, j);
                priQueue.add(lcp[i][j]);
            }
        }

        for (int cur = lineN; cur < lineN * 2 - 1; ++cur) {
            //System.out.println("[" + (nNode - linesN + 1) * 100.0 / (linesN - 1) + "%] finished.");

            Pattern maxPattern = null;
            while (!priQueue.isEmpty()) {
                maxPattern = priQueue.poll();
                if (invalid[maxPattern.lc] && invalid[maxPattern.rc]) {
                    maxPattern = null;
                    continue;
                }
                break;
            }
            if (maxPattern == null) break;

            patterns[cur] = maxPattern;
            invalid[maxPattern.lc] = invalid[maxPattern.rc] = true;

            //update the priQueue
            for (int i = 0; i < cur; ++i) {
                if (i == maxPattern.lc || i == maxPattern.rc) continue;
                lcp[i][cur] = getLcp(i, cur);
                priQueue.add(lcp[i][cur]);
            }

            // find all corpus consisting of pattern[cur]
            for (int i = 0; i < lineN; ++i) {
                //if (i == patterns[cur].lc || i == patterns[cur].rc) continue; //todo how efficient?
                if (isSubpattern(patterns[cur], patterns[i])) {
                    patterns[cur].sourceIndex.add(i);
                }
            }
            //System.out.println(patterns[cur]);
            if (patterns[cur].getSup() < minSup) continue;

            //filter overlay ones
            boolean repated = false;
            for (int i = 0; i < ret.size(); ++i) {
                if (isSubpattern(patterns[cur], ret.get(i)) || isSubpattern(ret.get(i), patterns[cur])) {
                    repated = true;
                    break;
                }
            }
            if (!repated && patterns[cur].size() > 0) {
                ret.add(patterns[cur]);
                //System.out.println(ret.get(ret.size() - 1));
            }
            invalid[cur] = true;
        }
        return ret;
    }


    //check whether A is substring of B or not, and update Wildcards
    private boolean isSubpattern(Pattern A, Pattern B) {
        if (A.size() == 0) return true;
        if (A.size() > B.size()) return false;
        if (B.getSourceId() < 0) { // cmp between pattens (equals to isSubstr)
            for (int i = 0, j = 0; i < A.size();) {
                while (j < B.size() && !B.get(j).equals(A.get(i))) ++j;
                if (B.size() <= j) {
                    return false;
                }
                ++i; ++j;
            }
            return true;
        }

        for (int i = 0; i < A.size() + 2; ++i)
            for (int j = 0; j < B.size() + 2; ++j) dp[i][j] = 0;
        dp[A.size() + 1][B.size() + 1] = 1;

        for (int i = A.size(); i >= 0; --i) {//A index
            if (i == A.size()) {
                StringBuffer sb = new StringBuffer("");
                for (int j = B.size(); j >= 1; sb.append(B.get(j - 1)), --j) {
                    if (!isValidWildcard(B, j, B.size() + 1, sb.toString())) continue;
                    if (A.get(i - 1).equals(B.get(j - 1))) {
                        dp[i][j] = 1;
                        path[i][j] = B.size() + 1;
                    }
                    //sb.append(B.get(j - 1));
                }
                continue;
            }
            for (int j = B.size(); j >= 0; --j) {// B index
                if (!(i == 0 && j == 0 || 0 < i && 0 < j && A.get(i - 1).equals(B.get(j - 1)))) continue;

                int res = 0;
                StringBuffer sb = new StringBuffer("");
                for (int nj = j + 1; nj <= B.size(); sb.append(B.get(nj - 1)), ++nj) {
                    if (!isValidWildcard(B, j, nj, sb.toString())) continue;
                    if (i + 1 <= A.size() && B.get(nj - 1).equals(A.get(i)) && dp[i + 1][nj] == 1) {
                        res = 1;
                        path[i][j] = nj;
                        break;
                    }
                    //sb.append(B.get(nj - 1));
                }
                dp[i][j] = res;
            }
        }
        if (dp[0][0] == 0) return false;

        //update wildcards info
        int j = 0;
        for (int i = 0; i <= A.size(); ++i) {
            StringBuilder sb = new StringBuilder("");
            for (int k = j + 1; k <= B.size() && k < path[i][j]; ++k) sb.append(B.get(k - 1));
            //todo
            A.updateWildcards(i, sb.toString(), B.getSourceId());
            j = path[i][j];
        }

        return true;
    }

    private Pattern getLcp(int ai, int bi) {
        Pattern cand = getLcs(ai, bi);
        if (cand.size() <= 0) return cand;
        Pattern A = patterns[ai], B = patterns[bi];

        //int N = cand.size() + 2, MA = A.size() + 2, MB = B.size() + 2;//extended the head and the tail

        /*
        int[][] dp1 = new int[N][MA];
        int[] len1 = new int[N];
        */
        for (int i = 0; i < cand.size() + 2; ++i) {
            lenA[i] = 0;
            for (int j = 0; j < A.size() + 2; ++j) dp[i][j] = 0;
        }
        getMaxLen(cand, A, dp, lenA);

        /*
        int[][] dp2 = new int[N][MB];
        int[] len2 = new int[N];
        */
        for (int i = 0; i < cand.size() + 2; ++i) {
            lenB[i] = 0;
            for (int j = 0; j < B.size() + 2; ++j) dp[i][j] = 0;
        }
        getMaxLen(cand, B, dp, lenB);

        for (int i = 1; i <= cand.size(); ++i) lenA[i] = Math.min(lenB[i], lenA[i]);
        int candi = 0;
        for (int i = 1; i <= cand.size(); ++i) if (lenA[i] > lenA[candi]) candi = i;

        List<String> nPatternList = new ArrayList<>();
        for (int i = candi; i < candi + lenA[candi] && i <= cand.size(); ++i) {
             nPatternList.add(cand.get(i - 1));
        }
        cand.setPattern(nPatternList);
        return cand;
    }

    private void getMaxLen(Pattern cand, Pattern A, int[][] dp, int[] len) {
        int N = cand.size() + 2, M = A.size() + 2;
        dp[N - 1][M - 1] = 1;
        for (int i = cand.size(); i >= 1; --i)
            for (int j = A.size(); j >= 1; --j) {
                if (!cand.get(i - 1).equals(A.get(j - 1))) continue;

                int res = 1;
                StringBuffer sb = new StringBuffer("");
                boolean find = false;
                for (int k = j + 1; k <= A.size(); sb.append(A.get(k - 1)), ++k) {
                    if (!isValidWildcard(A, j, k, sb.toString())) continue;
                    if (i + 1 <= cand.size() && A.get(k - 1).equals(cand.get(i)) && dp[i + 1][k] > 0) {
                        find = true;
                        res = Math.max(dp[i + 1][k] + 1, res);
                    }
                    //sb.append(A.get(k - 1));
                }
                if (!find) {
                    sb.setLength(0);
                    for (int k = j + 1; k <= A.size(); ++k) sb.append(A.get(k - 1));
                    if (!isValidWildcard(A, j, A.size() + 1, sb.toString())) res = -1;
                }
                dp[i][j] = res;
            }

        for (int i = 1; i <= cand.size(); ++i) {
            StringBuffer sb = new StringBuffer("");
            for (int j = 1; j <= A.size(); sb.append(A.get(j - 1)), ++j) {
                if (!isValidWildcard(A, 0, j, sb.toString())) continue;
                //sb.append(A.get(j - 1));
                len[i] = Math.max(dp[i][j], len[i]);
            }
        }
    }


    //token is valid in the context of (A[L], A[R])
    private boolean isValidWildcard(Pattern A, int L, int R, String wildcard) {
        //(L, [A], R), extended A's pattern[]
        if (wildcard == null || wildcard.length() == 0) return true;
        /*
        if (R + 1 <= A.size()) {
            if (A.get(R - 1).equals("月") && A.get(R).equals("租")) return true;
        }*/
        if (L == 0 || R == A.size() + 1) return invalidBoundWildcardDict.match(wildcard, 0).size() == 0;
        return invalidWildcardDict.match(wildcard, 0).size() == 0;
    }

    private Pattern getLcs(int ai, int bi) {
        Pattern A = patterns[ai], B = patterns[bi];
        for (int i = 1; i <= A.size(); ++i) {
            for (int j = 1; j <= B.size(); ++j) {
                int res = 0;
                if (A.get(i - 1).equals(B.get(j - 1))) res = Math.max(dp[i - 1][j - 1] + 1, res);
                else res = Math.max(dp[i - 1][j], dp[i][j - 1]);
                dp[i][j] = res;
            }
        }

        Pattern ret = new Pattern(new ArrayList<>(), new HashSet<>(), ai, bi, -1);

        for (int i = A.size(), j = B.size(); 0 < i && 0 < j; ) {
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

    public static void main(String[] args) throws IOException {

        //String corpusFilePath = "data/NLP/cluster.10010.txt";
        String corpusFilePath = "data/NLP/bug.txt";
        String filterDictPath = "data/NLP/invalidWildcardDict.txt";
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(corpusFilePath)));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(corpusFilePath + ".ext.2")));
        MiningPatterns miningPatterns = new MiningPatterns(0.3, filterDictPath);

        long start = System.currentTimeMillis();
        miningPatterns.initial(new FileReader(corpusFilePath));
        List<Pattern> ps = miningPatterns.getPatWithPosition();
        for (Pattern p : ps) {
            out.println(p.toString());
            System.out.println(p.toString());
        }
        /*
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            StringBuffer sb = new StringBuffer("");
            String clusterNo, phone;
            clusterNo = phone = null;
            while (line.length() > 0) {
                String[] tmp = line.split("\\t");
                clusterNo = tmp[0];
                phone = tmp[1];
                sb.append(tmp[2] + "\n");
                line = in.readLine();
            }
            in.readLine(); in.readLine();
            miningPatterns.initial(new StringReader(sb.toString()));
            List<Pattern> ps = miningPatterns.getPatWithPosition();
            for (Pattern p : ps) {
                out.println(clusterNo + "\t" + phone + "\t" + p.toString());
                System.out.println(clusterNo + "\t" + phone + "\t" + p.toString());
            }
        }
        */
        long stop = System.currentTimeMillis();

        out.close();
        System.out.println("Time Consumed: " + (stop - start) / 1000.0 + "s");
    }

}
