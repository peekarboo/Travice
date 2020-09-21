
package org.travice.helper;

import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class PatternUtil {

    private PatternUtil() {
    }

    public static class MatchResult {
        private String patternMatch;
        private String patternTail;
        private String stringMatch;
        private String stringTail;

        public String getPatternMatch() {
            return patternMatch;
        }

        public String getPatternTail() {
            return patternTail;
        }

        public String getStringMatch() {
            return stringMatch;
        }

        public String getStringTail() {
            return stringTail;
        }
    }

    public static MatchResult checkPattern(String pattern, String input) {

        if (!ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("-agentlib:jdwp")) {
            throw new RuntimeException("PatternUtil usage detected");
        }

        MatchResult result = new MatchResult();

        for (int i = 0; i < pattern.length(); i++) {
            try {
                Matcher matcher = Pattern.compile("(" + pattern.substring(0, i) + ").*").matcher(input);
                if (matcher.matches()) {
                    result.patternMatch = pattern.substring(0, i);
                    result.patternTail = pattern.substring(i);
                    result.stringMatch = matcher.group(1);
                    result.stringTail = input.substring(matcher.group(1).length());
                }
            } catch (PatternSyntaxException error) {
                Log.warning(error);
            }
        }

        return result;
    }

}
