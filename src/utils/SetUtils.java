package utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SetUtils {

    public static <T> Set<T> intersection(Set<T> A, Set<T> B) {
        Set<T> inter = new HashSet<T>(A);
        inter.retainAll(B);
        return inter;
    }

    public static <T> Set<T> union(Set<T> a, Set<T> b) {

        // Adding all elements of respective Sets
        // using addAll() method
        return new HashSet<T>() {
            {
                addAll(a);
                addAll(b);
            }
        };
    }

    public static Set<Integer> diff(Set<Integer> A, Set<Integer> B) {
        // A-B
        Set<Integer> dup_A = new HashSet<>(A);
        dup_A.removeAll(B);
        return dup_A;
    }
}
