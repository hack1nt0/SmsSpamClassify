package TC;

public class LightSwitchingPuzzle {
    public int minFlips(String state) {
        int N = state.length();
        int[] isPress = new int[N];
        for (int i = 0; i < N; ++i) {
            int toggleCnt = 0;
            for (int j = 0; j < i; ++j)
                if ((i + 1) % (j + 1) == 0) toggleCnt += isPress[j];
            int curState = state.charAt(i) == 'Y' ? 1 : 0;
            curState = (curState + toggleCnt) % 2;
            if (curState == 1) isPress[i] = 1;
        }
        int ret = 0;
        for (int i = 0; i < N; ++i) ret += isPress[i];
        return ret;
    }
}
