package edu.chalmers.sikkr.backend.util;

import org.apache.commons.lang3.StringUtils;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class for finding the closest matches to a string from a list of strings.
 */
public class FuzzySearchUtility {

    /**
     * Return a sorted list of the search elements that best match the given pattern,
     * with the best match first.
     * @param pattern
     * @param searchElements
     * @return
     */
    public static Set<String> getSearchResults(String pattern, Set<String> searchElements) {
        Set results = new TreeSet<SearchResult>();

        for(String str : searchElements) {
            int match = StringUtils.getLevenshteinDistance(pattern, str, 10);
            if(match >= 0) {
                SearchResult res = new SearchResult();
                res.name = str;
                res.match = match;
                results.add(res);
            }
        }
        return results;
    }

    static class SearchResult implements Comparable<SearchResult> {
        String name;
        int match;

        @Override
        public int compareTo(SearchResult another) {
            return Integer.compare(another.match, match);
        }
    }
}
