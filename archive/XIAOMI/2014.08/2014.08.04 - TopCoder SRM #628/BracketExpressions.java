package TC;

import java.util.Stack;

public class BracketExpressions {
    private String toBracket = "()[]{}";
    private int[] toInt = new int[128];
    String exp;

    public String ifPossible(String expression) {
        int MAXN = 0;
        exp = expression;
        for (char c: expression.toCharArray()) if (c == 'X') {MAXN++;}
        toInt['('] = 1;
        toInt[')'] = -1;
        toInt['['] = 2;
        toInt[']'] = -2;
        toInt['{'] = 3;
        toInt['}'] = -3;
        return BS(0, "") ? "possible" : "impossible";
    }

    private boolean BS(int cur, String tmp) {
        if (exp.length() <= cur)
            return valid(tmp.toString());
        if (exp.charAt(cur) == 'X') {
            for (int i = 0; i < toBracket.length(); ++i) {
                tmp += (toBracket.charAt(i));
                if (BS(cur + 1, tmp))
                    return true;
                tmp = tmp.substring(0, tmp.length() - 1);
            }
        } else {
            tmp += exp.charAt(cur);
            if (BS(cur + 1, tmp))
                return true;
            tmp = tmp.substring(0, tmp.length() - 1);
        }
        return false;
    }

    private boolean valid(String tmp) {
        Stack<Character> stack = new Stack<Character>();
        for (char c: tmp.toCharArray()) {
            if (toInt[c] > 0) {
                stack.push(c);
            }
            else if (!stack.isEmpty() && toInt[c] + toInt[stack.peek()] == 0) {
                stack.pop();
            }
            else
                return false;
        }
        return stack.isEmpty();
    }
}
