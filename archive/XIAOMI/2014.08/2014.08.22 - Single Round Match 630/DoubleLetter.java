package TC;

public class DoubleLetter {
    public String ableToSolve(String S) {
        int s, t;
        s = t = 0;
        for (int i = 0; i < S.length();) {
            if (i + 1 < S.length() && S.charAt(i) == S.charAt(i + 1)) {
                s = i;
                t = s + 1;
                while (t < S.length() && S.charAt(s) == S.charAt(t)) {
                    t++;
                    if (t - s == 2) {
                        S = S.substring(0, s) + S.substring(t, S.length());
                        break;
                    }
                }
                i = Math.max(s - 1, 0);
            }
            else {
                i++;
            }
        }
        return S.length() == 0 ? "Possible" : "Impossible";
    }
}
