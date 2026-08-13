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

import { StudyStatisticsDTO } from '../studies/shared/study.dto';

export interface InclusionsEvolution {
    labels: string[];
    examinations: number[];
    subjects: number[];
}

/**
 * importDate is missing for data imported before the field was introduced (january 2022),
 * in which case examinationDate is used as a fallback (same convention as the reference
 * Neurovasc-Dashboard analytics).
 */
function parseEffectiveDate(row: StudyStatisticsDTO): Date | null {
    const raw = row.importDate ?? row.examinationDate;
    if (!raw) return null;
    const date = new Date(raw);
    return isNaN(date.getTime()) ? null : date;
}

function minDatesById(rows: StudyStatisticsDTO[], idField: 'examinationId' | 'subjectId'): Date[] {
    const minById = new Map<number, Date>();
    for (const row of rows) {
        const id = row[idField];
        if (id == null) continue;
        const date = parseEffectiveDate(row);
        if (!date) continue;
        const existing = minById.get(id);
        if (!existing || date < existing) {
            minById.set(id, date);
        }
    }
    return Array.from(minById.values()).sort((a, b) => a.getTime() - b.getTime());
}

function buildMonthlyRange(start: Date, end: Date): Date[] {
    const points: Date[] = [];
    let cursor = new Date(start.getFullYear(), start.getMonth() + 1, 0); // end of start's month
    while (cursor <= end) {
        points.push(cursor);
        cursor = new Date(cursor.getFullYear(), cursor.getMonth() + 2, 0); // end of next month
    }
    if (points.length === 0 || points[points.length - 1] < end) {
        points.push(end);
    }
    return points;
}

/** sortedDates and points must both be sorted ascending. */
function cumulativeCounts(sortedDates: Date[], points: Date[]): number[] {
    const counts: number[] = [];
    let index = 0;
    for (const point of points) {
        while (index < sortedDates.length && sortedDates[index] <= point) {
            index++;
        }
        counts.push(index);
    }
    return counts;
}

function formatMonthLabel(date: Date): string {
    return date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
}

/**
 * Ports Neurovasc-Dashboard's analytics.py::get_inclusions_evolution to TypeScript:
 * cumulative count of distinct examinations and distinct subjects, by month, based on
 * each examination's/subject's earliest inclusion date across all its dataset rows.
 */
export function computeInclusionsEvolution(rows: StudyStatisticsDTO[]): InclusionsEvolution {
    const examDates = minDatesById(rows, 'examinationId');
    const subjectDates = minDatesById(rows, 'subjectId');

    if (examDates.length === 0 && subjectDates.length === 0) {
        return { labels: [], examinations: [], subjects: [] };
    }

    const start = [examDates[0], subjectDates[0]]
        .filter((d): d is Date => !!d)
        .reduce((min, d) => (d < min ? d : min));
    const end = new Date();

    const points = buildMonthlyRange(start, end);

    return {
        labels: points.map(formatMonthLabel),
        examinations: cumulativeCounts(examDates, points),
        subjects: cumulativeCounts(subjectDates, points),
    };
}
