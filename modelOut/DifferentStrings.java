
/**
 * Built using CHelper plug-in
 * Actual solution is at the top
 * @author DY
 */
public class DifferentStrings {
    public int minimize(String A, String B) {
        int ret = Integer.MAX_VALUE;
        for (int i = 0; i <= B.length() - A.length(); ++i) {
            int diff = 0;
            for (int j = 0; j < A.length(); ++j) if (A.charAt(j) != B.charAt(j + i)) ++diff;
            ret = Math.min(diff, ret);
        }
        return ret;
    }
}

