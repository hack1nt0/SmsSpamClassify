package TC;

public class BishopMove {
    public int howManyMoves(int r1, int c1, int r2, int c2) {
        if (r1 == r2 && c1 == c2)
            return 0;
        if (Math.abs(r1 - r2) == Math.abs(c1 - c2))
            return 1;
        if (hasCommonPoints(r1, c1, r2, c2) || hasCommonPoints(r2, c2, r1, c1))
            return 2;
        return -1;
    }

    private boolean hasCommonPoints(int r1, int c1, int r2, int c2) {
        int nr = (c2 + r2 + r1 - c1) / 2;
        int nc = c1 + nr - r1;
        return nc - c1 == nr - r1 && nc - c2 == r2 - nr && 0 <= nr && nr < 8 && 0 <= nc && nc < 8;
    }
}
