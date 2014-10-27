package TC;

public class RectangleCoveringEasy {
    public int solve(int holeH, int holeW, int boardH, int boardW) {
        int holeH1 = Math.min(holeH, holeW);
        int holeW1 = Math.max(holeH, holeW);
        int boardH1 = Math.min(boardH, boardW);
        int boardW1 = Math.max(boardH, boardW);
        int res = holeH1 <= boardH1 && holeW1 <= boardW1 && !(holeH1 == boardH1 && holeW1 == boardW1) ? 1 : -1;
        return res;
    }
}
