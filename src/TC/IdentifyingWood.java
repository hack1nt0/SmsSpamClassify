package TC;

public class IdentifyingWood {
    public String check(String s, String t) {
        for (int i = 0, j = 0;;) {
            while (i < s.length() && s.charAt(i) != t.charAt(j)) ++i;
            if (i >= s.length()) break;
            if (s.charAt(i) == t.charAt(j)) ++j;
            if (j >= t.length()) return "Yep, it's wood.";
            ++i;
        }
        return "Nope.";
    }
}
