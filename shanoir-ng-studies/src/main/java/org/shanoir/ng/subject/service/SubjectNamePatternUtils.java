/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.subject.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds subject names matching a study's subjectNamePattern. Only supports the shape produced
 * by the study form's pattern builder (study.component.ts buildSubjectNamePatternRegex()):
 * "^PREFIX(CENTER_SEGMENT)?SEP[charClass]{n}$", where PREFIX is an escaped literal or a
 * "(a|b)" group of custom names, and CENTER_SEGMENT is "SEP(c1|c2)" or "(SEP(c1|c2))?".
 */
public final class SubjectNamePatternUtils {

    /** Trailing "SEP[charClass]{n}$". */
    private static final Pattern SUFFIX = Pattern.compile("(\\\\.|[^\\\\])(\\[[^\\]]*\\])\\{(\\d+)\\}\\$$");

    private SubjectNamePatternUtils() { }

    /** The fixed part of a generated name and the length of the identifier to append to it. */
    public record NameBase(String base, int idLength) { }

    /**
     * Returns the part preceding the identifier ("PREFIX[SEP center]SEP") for a new subject of the
     * given center, or null when the pattern shape isn't recognized. The center prefix is only
     * used when the pattern lists it; the caller must check the final name against the pattern
     * (a mandatory center segment can't be satisfied without a listed center prefix).
     */
    public static NameBase computeNameBase(String pattern, String centerPrefix) {
        if (pattern == null || !pattern.startsWith("^")) {
            return null;
        }
        Matcher suffix = SUFFIX.matcher(pattern);
        if (!suffix.find()) {
            return null;
        }
        String escapedSeparator = suffix.group(1);
        String separator = unescape(escapedSeparator);
        int idLength = Integer.parseInt(suffix.group(3));
        String front = pattern.substring(1, pattern.length() - suffix.group(0).length());

        Map<Integer, Integer> openOf = matchParentheses(front);
        if (openOf == null) {
            return null;
        }
        String prefixPart = front;
        String centerAlternation = null;
        int last = front.length() - 1;
        if (front.endsWith(")?") && openOf.containsKey(last - 1)) {
            // optional center segment "(SEP(c1|c2))?"
            int open = openOf.get(last - 1);
            String inner = front.substring(open + 1, last - 1);
            if (!inner.startsWith(escapedSeparator)) {
                return null;
            }
            centerAlternation = inner.substring(escapedSeparator.length());
            prefixPart = front.substring(0, open);
        } else if (front.endsWith(")") && openOf.containsKey(last) && openOf.get(last) > 0) {
            // mandatory center segment "SEP(c1|c2)"; an alternation starting at 0 is the prefix itself
            int open = openOf.get(last);
            String beforeGroup = front.substring(0, open);
            if (!beforeGroup.endsWith(escapedSeparator)) {
                return null;
            }
            centerAlternation = front.substring(open);
            prefixPart = beforeGroup.substring(0, beforeGroup.length() - escapedSeparator.length());
        }

        String prefix = firstAlternative(prefixPart);
        if (prefix == null || prefix.isEmpty()) {
            return null;
        }
        StringBuilder base = new StringBuilder(prefix);
        if (centerAlternation != null && centerPrefix != null && !centerPrefix.isEmpty()
                && alternatives(centerAlternation).contains(centerPrefix)) {
            base.append(separator).append(centerPrefix);
        }
        base.append(separator);
        return new NameBase(base.toString(), idLength);
    }

    /** "(a|b)" -> "a" (unescaped), plain escaped literal -> unescaped literal. */
    private static String firstAlternative(String part) {
        if (part.startsWith("(") && part.endsWith(")")) {
            Map<Integer, Integer> openOf = matchParentheses(part);
            if (openOf != null && Integer.valueOf(0).equals(openOf.get(part.length() - 1))) {
                List<String> alternatives = alternatives(part);
                return alternatives.isEmpty() ? null : alternatives.get(0);
            }
        }
        return unescape(part);
    }

    /** Splits a "(a|b|c)" group on its top-level "|" and unescapes each alternative. */
    private static List<String> alternatives(String group) {
        List<String> result = new ArrayList<>();
        if (!group.startsWith("(") || !group.endsWith(")")) {
            return result;
        }
        String inner = group.substring(1, group.length() - 1);
        int depth = 0;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);
            if (c == '\\' && i + 1 < inner.length()) {
                current.append(c).append(inner.charAt(++i));
                continue;
            }
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (c == '|' && depth == 0) {
                result.add(unescape(current.toString()));
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        result.add(unescape(current.toString()));
        return result;
    }

    /** Maps each unescaped ")" index to its "(" index, or null if parentheses are unbalanced. */
    private static Map<Integer, Integer> matchParentheses(String s) {
        Map<Integer, Integer> openOf = new HashMap<>();
        Deque<Integer> stack = new ArrayDeque<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '(') {
                stack.push(i);
            } else if (c == ')') {
                if (stack.isEmpty()) {
                    return null;
                }
                openOf.put(i, stack.pop());
            }
        }
        return stack.isEmpty() ? openOf : null;
    }

    private static String unescape(String s) {
        return s.replaceAll("\\\\(.)", "$1");
    }
}
