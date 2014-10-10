package edu.chalmers.sikkr.backend.util;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
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

        if (pattern == null || pattern.trim().isEmpty()) {
            return null;
        }

        Set<SearchResult> matches = new TreeSet<SearchResult>();
        Set<SearchResult> discards = new TreeSet<SearchResult>();
        int topMatch = 1000;

        if (pattern == null || pattern.trim().isEmpty()) {
            return null;
        }
        
        for (String str : searchElements) {
            String element;

            /* Check if the search is only one word. If so, only compare with the first name of the contact. */
            if (pattern.split("\\s+").length == 1) {
                element = str.split("\\s+")[0];
            } else {
                element = str;
            }

            int match = StringUtils.getLevenshteinDistance(pattern.toLowerCase(), element.toLowerCase(), element.length()/3);
            LogUtility.writeLogFile(TAG, "Match between " + pattern + " and " + element + " is " + match);

            LogUtility.writeLogFile(TAG, "Match between " + pattern + " and " + element + " is " + match);
            if (match >= 0 && match <= topMatch) {
                if (match < topMatch) {
                    discards.addAll(matches);
                }

                topMatch = match;
                SearchResult res = new SearchResult();
                res.name = str;
                res.match = match;
                matches.add(res);
            }
        }

        matches.removeAll(discards);

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
            return Integer.compare(match, another.match);
        }
    }
}
