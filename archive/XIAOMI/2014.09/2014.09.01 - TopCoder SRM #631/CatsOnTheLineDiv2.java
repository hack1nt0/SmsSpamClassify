package TC;

import java.util.Arrays;
import java.util.Comparator;

public class CatsOnTheLineDiv2 {

    public String getAnswer(int[] position, int[] count, int time) {
        int N = position.length;
        Integer[] rank = new Integer[N];
        for (int i = 0; i < N; ++i) rank[i] = i;
        Arrays.sort(rank, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return position[o1] - position[o2];
            }
        });
        int validp = position[rank[0]] - time;
        for (int i = 0; i < N; ++i)
            for (int j = 0; j < count[rank[i]]; ++j) {
                if (Math.abs(validp++ - position[rank[i]]) > time) {
                    return "Impossible";
                }
            }
        /*
        int[] dl = new int[N], dr = new int[N];
        for (int i = 0; i < N; ++i) {
            dl[i] = i == 0 ? count[i] - 1 : calT(count[i] - 1, position[i - 1], position[i], dl[i - 1]);
            dr[i] = i == N - 1 ? count[i] - 1 : calT(count[i] - 1, position[i], position[i + 1], dr[i + 1]);
        }
        for (int sp = 0; sp < N; ++sp)
            for (int ln = 0; ln < count[sp]; ++ln) {
                int rn = count[sp] - ln - 1;
                int lt = sp == 0 ? ln : calT(ln, position[sp - 1], position[sp], dl[sp - 1]);
                int rt = sp == N - 1 ? rn : calT(rn, position[sp], position[sp + 1], dr[sp + 1]);
                System.out.println(Math.max(lt, rt));
                if (Math.max(lt, rt) <= time)
                    return "Possible";
            }
            */
        return "Possible";
    }

    private int calT(int left, int pl, int pr, int acc) {
        int res = 0;
        if (left < pr - pl - 1) {
            res = Math.max(acc, left);
        } else {
            res = Math.max(acc, pr - pl - 1) + left - (pr - pl - 1) + acc;
        }
        return res;
    }
}
