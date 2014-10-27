package TC;

import java.util.Arrays;

public class CircuitsConstruction {
    public int maximizeResistance(String circuit, int[] conductors) {
        int validX = count(circuit, 0)[0];
        Arrays.sort(conductors);
        int ans = 0;
        for (int i = 0; i < validX; ++i)
            ans += conductors[conductors.length - 1 - i];
        return ans;
    }

    private int[] count(String circuit, int cur) {
        /*
        if (circuit.length() <= cur)
            return new int[]{0, circuit.length()};
            */
        if (circuit.charAt(cur) == 'X')
            return new int[]{1, cur};
        int[] res = count(circuit, cur + 1);
        int lx = res[0];
        res = count(circuit, res[1] + 1);
        int rx = res[0];
        int curx = circuit.charAt(cur) == 'A' ? lx + rx : Math.max(lx, rx);
        //System.out.println(cur + ": " + curx);
        return new int[]{curx, res[1]};
    }
}
