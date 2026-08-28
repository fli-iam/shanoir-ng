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

// Alphanumeric or digital pool of characters to pick from
// when creating the Subject common name pattern example
const DIGIT_POOL = '84271937';
const ALNUM_POOL = 'A8f2K5qZAe93Bq7Ff1';

export interface PatternSuffixMatch {
    fullMatch: string;
    separator: string;
    charClass: string;
    length: number;
}

/**
 * Matches the trailing "SEPARATOR[CHARCLASS]{LENGTH}$" produced by
 * study.component.ts's buildSubjectNamePatternRegex(), and unescapes the
 * separator back to its literal form. Returns null if pattern is empty or
 * doesn't have this exact shape (e.g. a hand-written regex).
 */
export function matchPatternSuffix(pattern: string): PatternSuffixMatch | null {
    if (!pattern) return null;
    const match = pattern.match(/(\\.|[^\\])(\[[^\]]*\])\{(\d+)\}\$$/);
    if (!match) return null;
    const [fullMatch, rawSeparator, charClass, lengthStr] = match;
    return {
        fullMatch,
        separator: rawSeparator.replace(/\\(.)/g, '$1'),
        charClass,
        length: parseInt(lengthStr, 10)
    };
}

/**
 * Convenience wrapper around matchPatternSuffix() for callers that only need
 * the separator (e.g. prefilling a subject name during import).
 */
export function extractPatternSeparator(pattern: string): string | null {
    return matchPatternSuffix(pattern)?.separator ?? null;
}

/**
     * Takes a subpart of the pattern to replace any of its regex special characters
     * so that this subpart is interpreted only as raw text in the regex pattern. 
     * @param str 
     * @returns 
     */
export function escapeRegex(str: string): string {
    return (str ?? '').replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/**
     * Does the opposite as escapeRegex(), turns a regex into raw text elements to
     * be used as pattern example.
     * @param str 
     * @returns 
     */
export function unescapeRegex(str: string): string {
    return (str ?? '').replace(/\\(.)/g, '$1');
}

export function splitTopLevelAlternation(s: string): string[] {
    const parts: string[] = [];
    let depth = 0;
    let last = 0;
    for (let idx = 0; idx < s.length; idx++) {
        const ch = s[idx];
        if (ch == '\\') { idx++; continue; }
        if (ch == '(' || ch == '[') depth++;
        else if (ch == ')' || ch == ']') depth--;
        else if (ch == '|' && depth == 0) {
            parts.push(s.slice(last, idx));
            last = idx + 1;
        }
    }
    parts.push(s.slice(last));
    return parts;
}

export interface CenterPrefixSource {
    subjectNamePrefix?: string;
}

/**
 * Mirrors study.component.ts's buildCenterPrefixSegment(), as a pure function so it can be
 * reused to strip the center-prefix segment back out when decoding an existing pattern.
 */
function buildCenterPrefixSegment(separator: string, centers: CenterPrefixSource[]): string {
    const centerPrefixes = Array.from(new Set(centers.map(c => c.subjectNamePrefix).filter(p => p?.length > 0)));
    if (!centerPrefixes.length) return '';
    const someCenterHasNoPrefix = centers.some(c => !c.subjectNamePrefix?.length);
    const alternation = '(' + centerPrefixes.map(p => escapeRegex(p)).join('|') + ')';
    const unit = escapeRegex(separator) + alternation;
    return someCenterHasNoPrefix ? '(' + unit + ')?' : unit;
}

/**
 * Extracts the "Subject name prefix" literal from a persisted subjectNamePattern - the study
 * name, or the first configured custom prefix if that option was used instead - i.e. everything
 * before the optional center-prefix segment and the final separator+identifier. Needs the
 * study's center list to know what to strip, since the center segment isn't distinguishable
 * from the prefix segment by shape alone. Returns null if pattern is empty/unrecognized.
 */
export function extractPatternPrefix(pattern: string, centers: CenterPrefixSource[] = []): string | null {
    const suffix = matchPatternSuffix(pattern);
    if (!suffix) return null;

    let front = pattern.slice(1, pattern.length - suffix.fullMatch.length);
    const expectedCenterSegment = buildCenterPrefixSegment(suffix.separator, centers);
    if (expectedCenterSegment && front.endsWith(expectedCenterSegment)) {
        front = front.slice(0, front.length - expectedCenterSegment.length);
    }

    if (front.startsWith('(') && front.endsWith(')')) {
        const alternatives = splitTopLevelAlternation(front.slice(1, -1));
        return unescapeRegex(alternatives[0] ?? '');
    }
    return unescapeRegex(front);
}

/**
 * Builds one human-readable string matching the given regex, for display to users who
 * are not expected to read a raw pattern (e.g. the subject common name pattern configured on a
 * study). Only supports the constructs produced by study.component.ts pattern builder:
 * literals (incl. backslash-escaped), alternation groups "(a|b)", optional groups "(...)?"
 * (always rendered, so the example stays complete/realistic) and character classes with a
 * "{n}" quantifier. Alternations always resolve to their first option.
 */
export function regexExample(pattern: string): string {
    if (!pattern) return '';

    let i = 0;

    function parseGroup(): string {
        // called with `i` positioned right after the opening '('
        let depth = 1;
        const start = i;
        while (i < pattern.length && depth > 0) {
            if (pattern[i] == '\\') { i += 2; continue; }
            if (pattern[i] == '(') depth++;
            else if (pattern[i] == ')') depth--;
            if (depth > 0) i++;
        }
        const inner = pattern.slice(start, i);
        if (pattern[i] == ')') i++;
        if (pattern[i] == '?') i++; // optional group: always included in the example
        return regexExample(splitTopLevelAlternation(inner)[0]);
    }

    function parseCharClass(): string {
        i++; // skip '['
        let hasLetter = false;
        while (i < pattern.length && pattern[i] != ']') {
            if (/[A-Za-z]/.test(pattern[i])) hasLetter = true;
            i++;
        }
        if (pattern[i] == ']') i++;
        let count = 1;
        if (pattern[i] == '{') {
            const close = pattern.indexOf('}', i);
            if (close != -1) {
                const n = parseInt(pattern.slice(i + 1, close).split(',')[0], 10);
                if (!isNaN(n)) count = n;
                i = close + 1;
            }
        }
        const pool = hasLetter ? ALNUM_POOL : DIGIT_POOL;
        return pool.slice(0, count).padEnd(count, hasLetter ? 'x' : '0');
    }

    let result = '';
    while (i < pattern.length) {
        const c = pattern[i];
        if (c == '^' || c == '$') {
            i++;
        } else if (c == '(') {
            i++;
            result += parseGroup();
        } else if (c == '[') {
            result += parseCharClass();
        } else if (c == '\\') {
            result += pattern[i + 1] ?? '';
            i += 2;
        } else {
            result += c;
            i++;
        }
    }
    return result;
}
