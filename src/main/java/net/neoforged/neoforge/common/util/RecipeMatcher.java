package net.neoforged.neoforge.common.util;

import java.util.BitSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Utility for matching recipe ingredients to input stacks.
 */
public class RecipeMatcher {
    private RecipeMatcher() {}

    /**
     * Finds a mapping of inputs to predicates such that each predicate is satisfied
     * by exactly one input (bipartite matching).
     *
     * @param inputs     The available input items
     * @param predicates The predicates (recipe slots) to match
     * @param <T>        The input type
     * @return An int array where result[i] is the index of the input matched to predicate i,
     *         or null if no matching was found
     */
    public static <T> int[] findMatches(List<T> inputs, List<Predicate<T>> predicates) {
        int n = predicates.size();
        int m = inputs.size();
        if (m < n) return null;

        int[] match = new int[n];
        java.util.Arrays.fill(match, -1);
        int[] inputMatch = new int[m];
        java.util.Arrays.fill(inputMatch, -1);

        // Build adjacency
        boolean[][] adj = new boolean[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                adj[i][j] = predicates.get(i).test(inputs.get(j));
            }
        }

        // Hungarian-style augmenting path matching
        for (int i = 0; i < n; i++) {
            BitSet visited = new BitSet(m);
            if (!augment(i, adj, match, inputMatch, visited, m)) return null;
        }
        return match;
    }

    private static boolean augment(int pred, boolean[][] adj, int[] match, int[] inputMatch, BitSet visited, int m) {
        for (int j = 0; j < m; j++) {
            if (adj[pred][j] && !visited.get(j)) {
                visited.set(j);
                if (inputMatch[j] == -1 || augment(inputMatch[j], adj, match, inputMatch, visited, m)) {
                    match[pred] = j;
                    inputMatch[j] = pred;
                    return true;
                }
            }
        }
        return false;
    }
}
