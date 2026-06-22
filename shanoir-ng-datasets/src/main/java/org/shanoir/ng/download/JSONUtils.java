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

package org.shanoir.ng.download;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JSONUtils {

    private JSONUtils() { }

    /**
     * Determine if two JSONObjects are similar.
     * They must contain the same set of names which must be associated with
     * similar values.
     *
     * @param other The other JSONObject
     * @return true if they are equal
     */
    public static boolean equals(JSONObject o1, JSONObject o2) {
        try {
            if (!(o2 instanceof JSONObject)) {
                return false;
            }
            if (!o1.names().equals(((JSONObject) o2).names())) {
                return false;
            }
            JSONArray names1 = o1.names();
            for (int i = 0; i < names1.length(); i++) {
                String name = names1.getString(i);
                Object valueThis = o1.get(name);
                Object valueOther = o2.get(name);
                if (valueThis == valueOther) {
                    continue;
                }
                if (valueThis == null) {
                    return false;
                }
                if (valueThis instanceof JSONObject) {
                    if (!(valueOther instanceof JSONObject) || !equals((JSONObject) valueThis, (JSONObject) valueOther)) {
                        return false;
                    }
                } else if (valueThis instanceof JSONArray) {
                    if (!(valueOther instanceof JSONArray) || !equals((JSONArray) valueThis, (JSONArray) valueOther)) {
                        return false;
                    }
                } else if (valueThis instanceof Number && valueOther instanceof Number) {
                    if (!isNumberSimilar((Number) valueThis, (Number) valueOther)) {
                        return false;
                    }
                } else if (!valueThis.equals(valueOther)) {
                    return false;
                }
            }
            return true;
        } catch (Throwable exception) {
            return false;
        }
    }

    /**
     * Determine if two JSONArrays are similar.
     * They must contain similar sequences.
     *
     * @param other The other JSONArray
     * @return true if they are equal
     * @throws JSONException
     */
    public static boolean equals(JSONArray o1, JSONArray o2) throws JSONException {
        if (!(o2 instanceof JSONArray)) {
            return false;
        }
        int len = o1.length();
        if (len != o2.length()) {
            return false;
        }
        for (int i = 0; i < len; i += 1) {
            Object valueThis = o1.get(i);
            Object valueOther = o2.get(i);
            if (valueThis == valueOther) {
                continue;
            }
            if (valueThis == null) {
                return false;
            }
            if (valueThis instanceof JSONObject) {
                if (!(valueOther instanceof JSONObject) || !equals((JSONObject) valueThis, (JSONObject) valueOther)) {
                    return false;
                }
            } else if (valueThis instanceof JSONArray) {
                if (!(valueOther instanceof JSONArray) || !equals((JSONArray) valueThis, (JSONArray) valueOther)) {
                    return false;
                }
            } else if (valueThis instanceof Number && valueOther instanceof Number) {
                if (!isNumberSimilar((Number) valueThis, (Number) valueOther)) {
                    return false;
                }
            } else if (!valueThis.equals(valueOther)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two numbers to see if they are similar.
     *
     * If either of the numbers are Double or Float instances, then they are checked to have
     * a finite value. If either value is not finite (NaN or &#177;infinity), then this
     * function will always return false. If both numbers are finite, they are first checked
     * to be the same type and implement {@link Comparable}. If they do, then the actual
     * {@link Comparable#compareTo(Object)} is called. If they are not the same type, or don't
     * implement Comparable, then they are converted to {@link BigDecimal}s. Finally the
     * BigDecimal values are compared using {@link BigDecimal#compareTo(BigDecimal)}.
     *
     * @param l the Left value to compare. Can not be <code>null</code>.
     * @param r the right value to compare. Can not be <code>null</code>.
     * @return true if the numbers are similar, false otherwise.
     */
    private static boolean isNumberSimilar(Number l, Number r) {
        if (!numberIsFinite(l) || !numberIsFinite(r)) {
            // non-finite numbers are never similar
            return false;
        }
        // if the classes are the same and implement Comparable
        // then use the built in compare first.
        if (l.getClass().equals(r.getClass()) && l instanceof Comparable) {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            int compareTo = ((Comparable) l).compareTo(r);
            return compareTo == 0;
        }
        // BigDecimal should be able to handle all of our number types that we support through
        // documentation. Convert to BigDecimal first, then use the Compare method to
        // decide equality.
        final BigDecimal lBigDecimal = objectToBigDecimal(l, null, false);
        final BigDecimal rBigDecimal = objectToBigDecimal(r, null, false);
        if (lBigDecimal == null || rBigDecimal == null) {
            return false;
        }
        return lBigDecimal.compareTo(rBigDecimal) == 0;
    }

    /**
     * @param val value to convert
     * @param defaultValue default value to return is the conversion doesn't work or is null.
     * @param exact When <code>true</code>, then {@link Double} and {@link Float} values will be converted exactly.
     *      When <code>false</code>, they will be converted to {@link String} values before converting to {@link BigDecimal}.
     * @return BigDecimal conversion of the original value, or the defaultValue if unable
     *          to convert.
     */
    private static BigDecimal objectToBigDecimal(Object val, BigDecimal defaultValue, boolean exact) {
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }
        if (val instanceof BigInteger) {
            return new BigDecimal((BigInteger) val);
        }
        if (val instanceof Double || val instanceof Float) {
            if (!numberIsFinite((Number) val)) {
                return defaultValue;
            }
            if (exact) {
                return new BigDecimal(((Number) val).doubleValue());
            } else {
                // use the string constructor so that we maintain "nice" values for doubles and floats
                // the double constructor will translate doubles to "exact" values instead of the likely
                // intended representation
                return new BigDecimal(val.toString());
            }
        }
        if (val instanceof Long || val instanceof Integer
                || val instanceof Short || val instanceof Byte) {
            return new BigDecimal(((Number) val).longValue());
        }
        // don't check if it's a string in case of unchecked Number subclasses
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static boolean numberIsFinite(Number n) {
        if (n instanceof Double && (((Double) n).isInfinite() || ((Double) n).isNaN())) {
            return false;
        } else if (n instanceof Float && (((Float) n).isInfinite() || ((Float) n).isNaN())) {
            return false;
        }
        return true;
    }
}
