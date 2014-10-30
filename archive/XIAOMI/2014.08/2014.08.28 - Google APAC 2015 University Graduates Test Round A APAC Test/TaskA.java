package TC;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.PrintWriter;

public class TaskA {
    public void solve(int testNumber, Scanner in, PrintWriter out) {
        out.print("Case #" + testNumber + ": ");
        int n = in.nextInt();
        int[] d2Bin = toBinInt(new String[]{"1111110",
                                             "0110000",
                                             "1101101",
                                             "1111001",
                                             "0110011",
                                             "1011011",
                                             "1011111",
                                             "1110000",
                                             "1111111",
                                             "1111011"});
        int[] disp = new int[n];
        for (int i = 0; i < disp.length; ++i)
            disp[i] = toBinInt(in.next());
        List<String> ret = new ArrayList<>();
        for (int i = 9; i >= 0; --i) {
            boolean sat = true;
            int brokenLeds = 0;
            for (int j = 0; j < n; ++j) {
                int sug = d2Bin[((i - j) % 10 + 10) % 10];
                if ((disp[j] & brokenLeds) > 0 || !isSubset(disp[j] , sug)) {
                    sat = false;
                    break;
                }
                brokenLeds |= disp[j]  ^ sug;
            }
            if (sat) {
                int next = ((i - n) % 10 + 10) % 10;
                int res = d2Bin[next] & ~brokenLeds;
                String tmp = "";
                for (int j = 0; j < 7; ++j)
                    tmp += res >> 6 - j & 1;
                //modelOut.println(Integer.toBinaryString(ret));
                ret.add(tmp);
            }
        }
        if (ret.size() == 1)
            out.println(ret.get(0));
        else
            out.println("ERROR!");
    }

    private boolean isSubset(int a, int b) {
        return (a & b) + (a ^ b) == b;
    }

    private int[] toBinInt(String[] strings) {
        int[] res = new int[strings.length];
        for (int i = 0; i < res.length; ++i)
            res[i] = toBinInt(strings[i]);
        return res;
    }

    private int toBinInt(String str) {
        int res = 0;
        for (int j = 0; j < 7; ++j)
            res += (1 << j) * (str.charAt(6 - j) - '0');
        return res;
    }
}
