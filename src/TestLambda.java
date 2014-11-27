import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by dy on 14-11-24.
 */
class A {
    public String getName() {
        return "A";
    }
}

public class TestLambda {

    public static void main(String[] args) {
        Comparator<A> byName = Comparator.comparing(A::getName);
        A[] as = new A[0];
        Arrays.sort(as, byName);
    }
}
