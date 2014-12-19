package TC;

public class ForgetfulAddition {
    public int minNumber(String expression) {
        int ret = Integer.MAX_VALUE;
        for (int i = 1; i <= expression.length() - 1; ++i) {
            int a = Integer.valueOf(expression.substring(0, i));
            int b = Integer.valueOf(expression.substring(i));
            ret = Math.min(a + b, ret);
        }
        return ret;
    }
}
