package TC;

import java.util.*;

public class GoodSubset {
    class Pair {
        int cur;
        long left;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair pair = (Pair) o;

            if (cur != pair.cur) return false;
            if (left != pair.left) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = cur;
            result = 31 * result + (int) (left ^ (left >>> 32));
            return result;
        }

        Pair(int cur, long left) {
            this.cur = cur;
            this.left = left;
        }
    }
    long MOD = 1000000000 + 7;
    Map<Pair, Long> mem = new HashMap<>();
    public int numberOfSubsets(int goodValue, int[] d) {
        System.out.println(1 % 1);
        Arrays.sort(d);
        List<Integer> lst = new ArrayList<>();
        for (int i = 0; i < d.length; ++i) if (goodValue % d[i] == 0)
            lst.add(d[i]);
        long ret = dp(0, goodValue, lst);
        if (goodValue == 1) ret = Math.max(0, ret - 1);
        return (int)ret;
    }

    private long dp(int cur, long left, List<Integer> lst) {
        if (cur >= lst.size()) {
            return left == 1 ? 1 : 0;
        }
        Pair np = new Pair(cur, left);
        if (mem.containsKey(np))
            return mem.get(np);
        long ret = dp(cur + 1, left, lst);
        if (left % lst.get(cur) == 0)
            ret = (ret + dp(cur + 1, left / lst.get(cur), lst)) % MOD;
        mem.put(np, ret);
        return ret;
    }
}
