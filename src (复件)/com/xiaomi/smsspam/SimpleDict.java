package com.xiaomi.smsspam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleDict {
	
	private ArrayList<PairCount> mDict;
    private int mMaxDictLength;

    class Node{
        char ch;
        boolean flag;// true: is a new phrase
        double ig;   // information gain
        int num;     // total times appeared

        List<Node> next;
    }
    public static final int NORMAL = 0;
    public static final int NEW = 1;

    private List<Node> mRoot;

    public List<String>[] cut(String text){
        List<String>[] ret = new List[2];
        for(int i = 0; i < ret.length; ++i){
            ret[i] = new ArrayList<String>();
        }
        List<int[]> pos = new ArrayList<int[]>();
        List<Double> igs = new ArrayList<Double>();

        List<Node> nodes;
        for(int i = 0; i < text.length(); ++i){
            nodes = mRoot;
            // If there is "abcd" and "adb", we choose the longest one, "abcd"
            boolean jMatched = false;
            for(int j = i; j < text.length() && nodes != null; ++j){
                boolean match = false;
                for(Node n : nodes){
                    if(n.ch == text.charAt(j)){
                        nodes = n.next;
                        match = true;
                        if(n.flag){
                            if(jMatched){
                                //ret[NEW].remove(ret[NEW].size() - 1);
                                pos.remove(pos.size() - 1);
                                igs.remove(igs.size() - 1);
                            }
                            //ret[NEW].add(text.substring(i, j + 1));
                            pos.add(new int[]{i, j});
                            igs.add(n.ig);
                            jMatched = true;
                        }
                        break;
                    }
                }
                if(!match){
                    break;
                }
            }
        }

        // Remove the overwrited new phrase
        if(pos.size() > 1){
            int preIndex = 0;
            for(int i = 1; i < pos.size(); ++i){
                // curr Start <= pre End
                if(pos.get(i)[0] <= pos.get(preIndex)[1]){
                    int removeId = igs.get(i) < igs.get(preIndex) ? i : preIndex;
                    preIndex = igs.get(i) < igs.get(preIndex) ? preIndex : i;
                    pos.get(removeId)[0] = 0;
                    pos.get(removeId)[1] = 0;
                }else{
                    preIndex = i;
                }
            }
        }

        int lastPos = 0;
        for(int[] p : pos){
            int start = p[0];
            int end = p[1];
            if(start < end){
                if(lastPos < start){
                    ret[NORMAL].add(text.substring(lastPos, start));
                }
                ret[NEW].add(text.substring(start, end + 1));
                lastPos = end + 1;
            }
        }
        if(lastPos < text.length()){
            ret[NORMAL].add(text.substring(lastPos));
        }
        return ret;
    }

    public void load(Map<String, PairCount>[] phrases){
        mDict = new ArrayList<PairCount>();
        mMaxDictLength = 0;
        mRoot = new LinkedList<Node>();
        if(phrases == null){
            return;
        }
        for(Map<String, PairCount> map : phrases){
            Iterator<Map.Entry<String, PairCount>> it = map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, PairCount> entry = it.next();
                PairCount pc = entry.getValue();
                mDict.add(pc);
                if(pc.phs.length() > mMaxDictLength){
                    mMaxDictLength = pc.phs.length();
                }

                String phs = pc.phs;
                List<Node> nodes = mRoot;
                Node node = null;
                for(int i = 0; i < phs.length(); ++i){
                    node = null;
                    for(Node n : nodes){
                        if(n.ch == phs.charAt(i)){
                            nodes = n.next;
                            node = n;
                            break;
                        }
                    }
                    if(node == null){
                        for(int j = i; j < phs.length(); ++j){
                            node = new Node();
                            node.ch = phs.charAt(j);
                            if(j == phs.length() - 1){
                                node.flag = true;
                                node.ig = pc.ig;
                                node.num = pc.vals[0] + pc.vals[1];
                            }
                            node.next = new LinkedList<Node>();
                            nodes.add(node);
                            nodes = node.next;
                        }
                    }
                }
            }
        }
    }
}
