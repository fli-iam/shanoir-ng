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

package org.shanoir.uploader.utils;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Java port of the parts of shanoir-ng-front's regex-example.util.ts that ShanoirUploader needs
 * to validate a manually entered subject common name at import time, so its ImportForm behaves
 * like the web import flow.
 * Only supports the constructs produced by study.component.ts's pattern builder
 * ("^PREFIX(SEP(idxA|idxB))?SEP[charClass]{n}$"). ImportFromTable does not use this.
 */
public final class SubjectNamePatternUtils {

    private SubjectNamePatternUtils() { }

    /** Matches the trailing "SEP[charClass]{n}$" produced by buildSubjectNamePatternRegex(). */
    private static final Pattern SUFFIX = Pattern.compile("(\\\\.|[^\\\\])(\\[[^\\]]*\\])\\{(\\d+)\\}\\$$");

    /**
     * True when the name matches the pattern, or when there is no usable pattern (never blocks
     * then - mirrors the backend's checkSubjectNamePattern() which ignores malformed patterns).
     */
    public static boolean nameMatchesPattern(String name, String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return true;
        }
        if (name == null) {
            return false;
        }
        try {
            return name.matches(pattern);
        } catch (PatternSyntaxException e) {
            return true;
        }
    }

    /**
     * Whether the persisted pattern actually weaves in the study's per-center prefixes
     * (i.e. "Use center index / prefix" was ticked on the study). Mirrors
     * regex-example.util.ts's patternUsesCenterPrefix().
     *
     * @param centerPrefixes       the non-empty subjectNamePrefix of every study center
     * @param someCenterHasNoPrefix true if at least one study center has no prefix (then the
     *                              segment is optional in the pattern: "(SEP(...))?")
     */
    public static boolean patternUsesCenterPrefix(String pattern, List<String> centerPrefixes, boolean someCenterHasNoPrefix) {
        String segment = buildCenterPrefixSegment(pattern, centerPrefixes, someCenterHasNoPrefix);
        if (segment == null) {
            return false;
        }
        Matcher m = SUFFIX.matcher(pattern);
        if (!m.find()) {
            return false;
        }
        String front = pattern.substring(1, pattern.length() - m.group(0).length());
        return front.endsWith(segment);
    }

    /**
     * Whether the name carries the prefix of one of the allowed centers, in the position the
     * pattern expects it ("SEP<allowedPrefix>SEP<identifier>$"). Mirrors the
     * allowedCenterSegmentRegex check of subject.component.ts. Returns true (does not block)
     * when there is nothing to enforce or the pattern shape is not recognized.
     */
    public static boolean nameUsesAllowedCenterPrefix(String name, String pattern, List<String> allowedCenterPrefixes) {
        if (name == null || allowedCenterPrefixes == null || allowedCenterPrefixes.isEmpty()) {
            return true;
        }
        Matcher m = SUFFIX.matcher(pattern);
        if (!m.find()) {
            return true;
        }
        String separator = unescape(m.group(1));
        String charClass = m.group(2);
        String length = m.group(3);
        String alternation = "(" + allowedCenterPrefixes.stream()
                .map(SubjectNamePatternUtils::escape).collect(Collectors.joining("|")) + ")";
        String regex = escape(separator) + alternation + escape(separator) + charClass + "{" + length + "}$";
        try {
            return Pattern.compile(regex).matcher(name).find();
        } catch (PatternSyntaxException e) {
            return true;
        }
    }

    /** Mirrors regex-example.util.ts's buildCenterPrefixSegment(). Returns null when no center has a prefix. */
    private static String buildCenterPrefixSegment(String pattern, List<String> centerPrefixes, boolean someCenterHasNoPrefix) {
        List<String> distinct = centerPrefixes == null ? Collections.emptyList()
                : centerPrefixes.stream()
                        .filter(p -> p != null && !p.isEmpty())
                        .distinct()
                        .collect(Collectors.toList());
        if (distinct.isEmpty()) {
            return null;
        }
        Matcher m = SUFFIX.matcher(pattern);
        if (!m.find()) {
            return null;
        }
        String separator = unescape(m.group(1));
        String alternation = "(" + distinct.stream()
                .map(SubjectNamePatternUtils::escape).collect(Collectors.joining("|")) + ")";
        String unit = escape(separator) + alternation;
        return someCenterHasNoPrefix ? "(" + unit + ")?" : unit;
    }

    /** Same escaped set as regex-example.util.ts's escapeRegex(). */
    private static String escape(String s) {
        return s == null ? "" : s.replaceAll("([.*+?^${}()|\\[\\]\\\\])", "\\\\$1");
    }

    private static String unescape(String s) {
        return s == null ? "" : s.replaceAll("\\\\(.)", "$1");
    }
}
