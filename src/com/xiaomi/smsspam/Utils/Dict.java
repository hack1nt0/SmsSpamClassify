package com.xiaomi.smsspam.Utils;

import java.io.*;
import java.util.*;

/**
 * Created by root on 14-9-5.
 */
public abstract class Dict {

    public abstract int contains(char[] token, int L, int R);

    public int contains(char[] token) {
        return this.contains(token, 0, token.length);
    }

    public abstract float getLogFreq(int index);

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

class AlphabeticalOrderArray extends Dict{

    int MAXN = 350000;
    char[][] arrs = new char[MAXN][];
    float[] logFreq = new float[MAXN];
    int realSize = 0;

    public AlphabeticalOrderArray(String filePath) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/jieba.dict.utf8")));
            long beforeM = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long beforeT = System.currentTimeMillis();
            long totCharN = 0;
            while (true) {
                String line = in.readLine();
                if (line == null) break;
                String[] vecBuf = line.split(" ");
                if (2 > vecBuf.length) {
                    continue;
                }
                if (4 < vecBuf.length) {
                    continue;
                }
                arrs[realSize] = vecBuf[0].toCharArray();
                totCharN += arrs[realSize].length * 2;
                logFreq[realSize] = Float.valueOf(vecBuf[1]);
                totCharN += 4;
                if (3 <= vecBuf.length) {
                    //token.tag = vecBuf[2];
                }
                ++realSize;
            }
            in.close();
            System.gc();
            long afterM = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long afterT = System.currentTimeMillis();
            double usedM = (afterM - beforeM) / 1024.0 / 1024;
            double usedT = (afterT - beforeT) / 1000.0;
            System.out.println("Consumed Time: " + usedT + "s");
            System.out.println("Consumed Mem: " + usedM + "MB");
            System.out.println("Consumed Real Mem: " + (totCharN / 1024.0 / 1024 / 8) + "MB");
            System.out.println(arrs[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int contains(char[] token, int L, int R) {
        int l = 0, r = realSize;
        while (l < r) {
            int mid = l + (r - l) / 2;
            int cmp = charArrCmp(token, L, R, arrs[mid]);
            switch (cmp) {
                case 0: return mid;
                case 1: l = mid + 1;
                case -1: r = mid;
            }
        }
        return -1;
    }

    @Override
    public float getLogFreq(int index) {
        return logFreq[index];
    }

    private int charArrCmp(char[] A, int L, int R, char[] B) {
        for (int i = 0; i < R - L || i < B.length; ++i) {
            if (i >= B.length || A[i + L] > B[i]) return 1;
            if (i >= R - L || A[i + L] < B[i]) return -1;
        }
        return 0;
    }
}

class Trie extends Dict{

    public Trie() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("data/jieba.dict.utf8")));
            long beforeM = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long beforeT = System.currentTimeMillis();

            while (true) {
                String line = in.readLine();
                if (line == null) break;
                Token token = new Token();
                String[] vecBuf = line.split(" ");
                if (2 > vecBuf.length) {
                    continue;
                }
                if (4 < vecBuf.length) {
                    continue;
                }
                token.what = vecBuf[0].toCharArray();
                token.freq = Integer.valueOf(vecBuf[1]);
                if (3 <= vecBuf.length) {
                    //token.tag = vecBuf[2];
                }
                this.insert(token);
            }
            in.close();
            System.gc();
            long afterM = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long afterT = System.currentTimeMillis();
            double usedM = (afterM - beforeM) / 1024.0 / 1024;
            double usedT = (afterT - beforeT) / 1000.0;
            System.out.println("Consumed Time: " + usedT + "s");
            System.out.println("Consumed Mem: " + usedM + "MB");
            System.out.println(this.root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class MyArrayList<T> {
        Object[] arr;
        int defaultLen = 1;
        int realSize = 0;

        public MyArrayList() {
            arr = new Object[defaultLen];
        }

        public T get(int index) {
            return (T) arr[index];
        }

        public void add(int index, T nElement) {
            if (index >= arr.length || realSize >= arr.length) {
                Object[] nArr = new Object[arr.length + 1];
                for (int i = 0; i < index; ++i) nArr[i] = arr[i];
                nArr[index] = nElement;
                for (int i = index; i < realSize; ++i) nArr[i + 1] = arr[i];
                arr = nArr;
            } else {
                for (int i = realSize; i > index; --i) arr[i] = arr[i - 1];
                arr[index] = nElement;
            }
            realSize += 1;
        }

        public int size() {
            return realSize;
        }
    }

    static class Token {
        char[] what;
        int freq;
        //double logFreq;
        //String tag;
    }

    class TrieNode {
        char what;
        //Map<Character, TrieNode> childs;
        //List<TrieNode> childs;
        MyArrayList<TrieNode> childs;
        Token token;

        public TrieNode() {
            //childs = new HashMap<>();
            //childs = new ArrayList<>();
            token = null;
        }

        public TrieNode(char what) {
            //childs = new HashMap<>();
            //childs = new ArrayList<>();
            token = null;
            this.what = what;
        }

        public Map<Character, TrieNode> getNewChilds(int x, int y) {
            return new HashMap<>();
        }

        public List<TrieNode> getNewChilds(int x) {
            return new ArrayList<>();
        }

        public MyArrayList<TrieNode> getNewChilds() {
            return new MyArrayList<>();
        }

        public boolean equals(TrieNode obj) {
            return what == obj.what;
        }

        @Override
        public int hashCode() {
            return what;
        }
    }

    TrieNode root = new TrieNode();

    public void insert(Token token) {
        TrieNode cur = root;
        for (int i = 0; i < token.what.length; ++i) {
            TrieNode nNode = new TrieNode(token.what[i]);
            if (canFind(cur.childs, nNode)) {
                cur = index(cur.childs, nNode);
                continue;
            }
            if (cur.childs == null) cur.childs = cur.getNewChilds(); //TODo ugly
            addNChild(cur.childs, nNode);
            cur = nNode;
        }
        //cur.token = token;
    }

    private void addNChild(Map<Character, TrieNode> childs, TrieNode nNode) {
        childs.put(nNode.what, nNode);
    }

    private TrieNode index(Map<Character, TrieNode> childs, TrieNode nNode) {
        return childs.get(nNode.what);
    }

    private boolean canFind(Map<Character, TrieNode> childs, TrieNode token) {
        if (childs == null) return false;
        return childs.containsKey(token.what);
    }

    private void addNChild(List<TrieNode> childs, TrieNode nNode) {
        childs.add(lowerBound(childs, nNode), nNode);
    }

    private TrieNode index(List<TrieNode> childs, TrieNode nNode) {
        return childs.get(lowerBound(childs, nNode));
    }

    private boolean canFind(List<TrieNode> childs, TrieNode token) {
        if (childs == null) return false;
        int lb = lowerBound(childs, token);
        int rb = upperBound(childs, token);
        return lb < rb;
    }

    private void addNChild(MyArrayList<TrieNode> childs, TrieNode nNode) {
        childs.add(lowerBound(childs, nNode), nNode);
    }

    private TrieNode index(MyArrayList<TrieNode> childs, TrieNode nNode) {
        return childs.get(lowerBound(childs, nNode));
    }

    private boolean canFind(MyArrayList<TrieNode> childs, TrieNode token) {
        if (childs == null) return false;
        int lb = lowerBound(childs, token);
        int rb = upperBound(childs, token);
        return lb < rb;
    }

    private int lowerBound(MyArrayList<TrieNode> childs, TrieNode token) {
        int l = 0, r = childs.size();
        while (l < r) {
            int mid = l + (r - l) / 2;
            if (token.what > childs.get(mid).what) l = mid + 1;
            else r = mid;
        }
        return l;
    }

    private int upperBound(MyArrayList<TrieNode> childs, TrieNode token) {
        int l = 0, r = childs.size();
        while (l < r) {
            int mid = l + (r - l) / 2;
            if (token.what >= childs.get(mid).what) l = mid + 1;
            else r = mid;
        }
        return l;
    }

    private int lowerBound(List<TrieNode> childs, TrieNode token) {
        int l = 0, r = childs.size();
        while (l < r) {
            int mid = l + (r - l) / 2;
            if (token.what > childs.get(mid).what) l = mid + 1;
            else r = mid;
        }
        return l;
    }

    private int upperBound(List<TrieNode> childs, TrieNode token) {
        int l = 0, r = childs.size();
        while (l < r) {
            int mid = l + (r - l) / 2;
            if (token.what >= childs.get(mid).what) l = mid + 1;
            else r = mid;
        }
        return l;
    }


    public int contains(char[] token, int L, int R) {
        TrieNode cur = root;
        for (int i = L; i < R; ++i) {
            TrieNode nNode = new TrieNode(token[i]);
            if (!canFind(cur.childs, nNode))
                return -1;
            cur = index(cur.childs, nNode);
        }
        return cur.what;
    }

    @Override
    public float getLogFreq(int index) {
        return 0; //TODO
    }

}