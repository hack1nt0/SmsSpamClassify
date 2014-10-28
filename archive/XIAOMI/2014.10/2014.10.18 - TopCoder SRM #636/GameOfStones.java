package TC;

public class GameOfStones {
    public int count(int[] stones) {
        int avr = 0;
        for (int x: stones) avr += x;
        if (avr % stones.length != 0) return -1;
        avr /= stones.length;
        int ret = 0;
        for (int x: stones) {
            if (Math.abs(x - avr) % 2 != 0) return -1;
            ret += Math.abs(x - avr) / 2;
        }
        return ret / 2;
    }
}
