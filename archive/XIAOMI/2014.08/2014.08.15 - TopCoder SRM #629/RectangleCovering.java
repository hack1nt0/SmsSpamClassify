package TC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RectangleCovering {
    public int minimumNumber(int holeH, int holeW, final int[] boardH, final int[] boardW) {
        ArrayList<Integer> A = new ArrayList<Integer>();
        for (int i = 0; i < boardH.length; ++i) {
            int bh = boardH[i], bw = boardW[i];
            if (holeH >= Math.max(bh, bw))
                continue;
            A.add(i);
            if (holeH < Math.min(bh, bw)) {
                boardW[i] = Math.max(bh, bw);
                boardH[i] = Math.min(bh, bw);
            }
            else if (holeH < bw) {
                boardH[i] = bw;
                boardW[i] = bh;
            }
        }
        int ans = Integer.MAX_VALUE;
        Collections.sort(A, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return boardW[o1] - boardW[o2];
            }
        });
        for (int i = 0, accw = 0; i < A.size(); ++i) {
            accw += boardW[A.get(A.size() - 1 - i)];
            if (accw >= holeW) {
                ans = Math.min(i + 1, ans);
                break;
            }
        }

        //B
        ArrayList<Integer> B = new ArrayList<Integer>();
        for (int i = 0; i < boardW.length; ++i) {
            int bh = boardH[i], bw = boardW[i];
            if (holeW >= Math.max(bh, bw))
                continue;
            B.add(i);
            if (holeW < Math.min(bh, bw)) {
                boardH[i] = Math.max(bh, bw);
                boardW[i] = Math.min(bh, bw);
            }
            else if (holeW < bh) {
                boardW[i] = bh;
                boardH[i] = bw;
            }
        }
        Collections.sort(B, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return boardH[o1] - boardH[o2];
            }
        });
        for (int i = 0, acch = 0; i < B.size(); ++i) {
            acch += boardH[B.get(B.size() - 1 - i)];
            if (acch >= holeH) {
                ans = Math.min(i + 1, ans);
                break;
            }
        }
        ans = ans == Integer.MAX_VALUE ? -1 : ans;
        return ans;
    }
}
