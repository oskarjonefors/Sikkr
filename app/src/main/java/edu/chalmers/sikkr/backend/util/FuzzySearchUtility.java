package edu.chalmers.sikkr.backend.util;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A class for finding the closest matches to a string from a list of strings.
 */
public class FuzzySearchUtility {

    public static final String TAG = "FuzzySearchUtility";

    /**
     * Return a sorted list of the search elements that best match the given pattern,
     * with the best match first. If no suitable results are found, return results;null is returned.
     * @param pattern
     * @param searchElements
     * @return
     */
    public static List<String> getSearchResults(String pattern, Set<String> searchElements) {
        Set<SearchResult> matches = new TreeSet<SearchResult>();

        for (String str : searchElements) {
            int match = StringUtils.getLevenshteinDistance(pattern, str, 10);
            Log.d(TAG, "Match between " + pattern + " and " + str + " is " + match);
            if (match >= 0) {
                SearchResult res = new SearchResult();
                res.name = str;
                res.match = match;
                matches.add(res);
            }
        }
        if (matches.size() <= 0) {
            return null;
        }

        List<String> results = new ArrayList<String>();
        for (SearchResult res : matches) {
            results.add(res.name);
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
