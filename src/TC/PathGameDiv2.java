package TC;

public class PathGameDiv2 {
    public int calc(String[] board) {
        int ret = 0, totWhite = 0;
        for (String r: board)
            for (char c: r.toCharArray()) totWhite += c == '.' ? 1 : 0;
        for (int i = 0; i < board.length; ++i) {
            int curR = i;
            if (board[curR].charAt(0) == '#') continue;
            int whiteN = 1;
            for (int j = 1; j < board[i].length();) {
                if (board[curR].charAt(j) == '#') curR = 1 - curR;
                else ++j;
                ++whiteN;
            }
            ret = Math.max(totWhite - whiteN, ret);
        }
        return ret;
    }
}
