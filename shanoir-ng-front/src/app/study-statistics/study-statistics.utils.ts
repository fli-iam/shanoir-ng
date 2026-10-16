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

export interface GlobalStatistics {
    centers: number;
    subjects: number;
    examinations: number;
}

function countDistinct(rows: StudyStatisticsDTO[], idField: 'centerId' | 'subjectId' | 'examinationId'): number {
    const ids = new Set<number>();
    for (const row of rows) {
        const id = row[idField];
        if (id != null) ids.add(id);
    }
    return ids.size;
}

/**
 * Global statistics: the number of distinct centers, subjects and examinations
 * that have at least one dataset in this study.
 */
export function computeGlobalStatistics(rows: StudyStatisticsDTO[]): GlobalStatistics {
    return {
        centers: countDistinct(rows, 'centerId'),
        subjects: countDistinct(rows, 'subjectId'),
        examinations: countDistinct(rows, 'examinationId'),
    };
}

export interface CenterBarData {
    centers: string[];
    counts: number[];
}

function computeDistinctCountByCenter(rows: StudyStatisticsDTO[], idField: 'examinationId' | 'subjectId'): CenterBarData {
    const idsByCenter = new Map<number, { name: string, ids: Set<number> }>();
    for (const row of rows) {
        if (row.centerId == null) continue;
        let entry = idsByCenter.get(row.centerId);
        if (!entry) {
            entry = { name: row.centerName, ids: new Set<number>() };
            idsByCenter.set(row.centerId, entry);
        }
        const id = row[idField];
        if (id != null) entry.ids.add(id);
    }

    const sorted = Array.from(idsByCenter.values())
        .map(entry => ({ name: entry.name, count: entry.ids.size }))
        .sort((a, b) => b.count - a.count);

    return {
        centers: sorted.map(entry => entry.name),
        counts: sorted.map(entry => entry.count),
    };
}

/**
 * Ports Neurovasc-Dashboard's analytics.py::get_exams_by_centers to TypeScript: the
 * number of distinct examinations per center, sorted with the largest contributor first.
 */
export function computeExamsByCenter(rows: StudyStatisticsDTO[]): CenterBarData {
    return computeDistinctCountByCenter(rows, 'examinationId');
}

/**
 * Ports Neurovasc-Dashboard's analytics.py::get_subjects_by_centers to TypeScript: the
 * number of distinct subjects per center, sorted with the largest contributor first.
 */
export function computeSubjectsByCenter(rows: StudyStatisticsDTO[]): CenterBarData {
    return computeDistinctCountByCenter(rows, 'subjectId');
}

// Distinct center names in the study, sorted alphabetically, for the "Select center" dropdown.
export function computeCenterNames(rows: StudyStatisticsDTO[]): string[] {
    const names = new Set<string>();
    for (const row of rows) {
        if (row.centerName) names.add(row.centerName);
    }
    return Array.from(names).sort((a, b) => a.localeCompare(b));
}

const CENTER_GREY_COLOR = '#c9c9c9';

/**
 * Center highlights behavior: when a center is selected, every
 * other center's bar is greyed out instead of being filtered out of the chart.
 */
export function centerHighlightColors(centers: string[], selectedCenter: string | null | undefined, activeColor: string): string[] {
    if (!selectedCenter) return centers.map(() => activeColor);
    return centers.map(center => center === selectedCenter ? activeColor : CENTER_GREY_COLOR);
}

// Uses the app's home page palette
// one hue per modality, in fixed order, any rarer modality not in this map
// falls back to a shared neutral "Other" grey, so a color is never
// reassigned depending on which modalities happen to be present.
const MODALITY_COLORS: Record<string, string> = {
    Mr: '#5F0F4E',
    Meg: '#67AECA',
    Ct: '#675682',
    Spect: '#E52A6F',
    Pet: '#FFAA00',
};
const OTHER_MODALITY_COLOR = '#8a8a8a';

function modalityColor(modality: string): string {
    return MODALITY_COLORS[modality] ?? OTHER_MODALITY_COLOR;
}

export interface ModalityBarData {
    modalities: string[];
    counts: number[];
    colors: string[];
}

/**
 * Compute datasets DICOM modality ratio: the number of
 * datasets per DICOM modality, sorted with the most common modality first.
 */
export function computeDatasetsByModality(rows: StudyStatisticsDTO[]): ModalityBarData {
    const countsByModality = new Map<string, number>();
    for (const row of rows) {
        const modality = row.modality ?? 'Unknown';
        countsByModality.set(modality, (countsByModality.get(modality) ?? 0) + 1);
    }

    const sorted = Array.from(countsByModality.entries())
        .map(([modality, count]) => ({ modality, count }))
        .sort((a, b) => b.count - a.count);

    return {
        modalities: sorted.map(entry => entry.modality),
        counts: sorted.map(entry => entry.count),
        colors: sorted.map(entry => modalityColor(entry.modality)),
    };
}

export interface ModalitySeries {
    modality: string;
    color: string;
    counts: number[];
}

export interface ModalityByCenterData {
    centers: string[];
    series: ModalitySeries[];
}

/**
 * Compute the number of datasets by DICOM modalities and centers.
 */
export function computeModalityByCenter(rows: StudyStatisticsDTO[]): ModalityByCenterData {
    const centerTotals = new Map<number, { name: string, total: number }>();
    const cellCounts = new Map<string, number>();
    const modalities = new Set<string>();

    for (const row of rows) {
        if (row.centerId == null) continue;
        const modality = row.modality ?? 'Unknown';
        modalities.add(modality);

        const key = row.centerId + '|' + modality;
        cellCounts.set(key, (cellCounts.get(key) ?? 0) + 1);

        let centerEntry = centerTotals.get(row.centerId);
        if (!centerEntry) {
            centerEntry = { name: row.centerName, total: 0 };
            centerTotals.set(row.centerId, centerEntry);
        }
        centerEntry.total++;
    }

    const sortedCenters = Array.from(centerTotals.entries()).sort((a, b) => b[1].total - a[1].total);
    const centers = sortedCenters.map(([, entry]) => entry.name);
    const centerIds = sortedCenters.map(([id]) => id);

    // fixed order: known modalities first (per MODALITY_COLORS declaration order), then any other
    const knownOrder = Object.keys(MODALITY_COLORS);
    const orderedModalities = [
        ...knownOrder.filter(m => modalities.has(m)),
        ...Array.from(modalities).filter(m => !knownOrder.includes(m)).sort(),
    ];

    const series: ModalitySeries[] = orderedModalities.map(modality => ({
        modality,
        color: modalityColor(modality),
        counts: centerIds.map(centerId => cellCounts.get(centerId + '|' + modality) ?? 0),
    }));

    return { centers, series };
}

// Quality uses good/warning/critical colors rather than the app's categorical palette.
const QUALITY_COLORS: Record<string, string> = {
    Valid: '#0ca30c',
    Warning: '#fab219',
    Error: '#d03b3b',
};
const MISSING_QUALITY = 'missing';
const MISSING_QUALITY_COLOR = '#8a8a8a';

function qualityColor(quality: string): string {
    return QUALITY_COLORS[quality] ?? MISSING_QUALITY_COLOR;
}

/**
 * Quality tags live on datasetAcquisition, not on the
 * subject, so this groups by datasetAcquisitionId.
 */
function qualityByAcquisition(rows: StudyStatisticsDTO[]): Map<number, { centerId: number, centerName: string, quality: string }> {
    const result = new Map<number, { centerId: number, centerName: string, quality: string }>();
    for (const row of rows) {
        if (row.datasetAcquisitionId == null) continue;
        if (!result.has(row.datasetAcquisitionId)) {
            result.set(row.datasetAcquisitionId, {
                centerId: row.centerId,
                centerName: row.centerName,
                quality: row.quality ?? MISSING_QUALITY,
            });
        }
    }
    return result;
}

export interface QualityDistributionData {
    qualities: string[];
    counts: number[];
    colors: string[];
}

/**
 * Compute Quality tags distribution among dataset acquisitions as a donut
 * chart: the number of dataset acquisitions (DICOM series) per quality tag.
 */
export function computeQualityDistribution(rows: StudyStatisticsDTO[]): QualityDistributionData {
    const countsByQuality = new Map<string, number>();
    for (const { quality } of qualityByAcquisition(rows).values()) {
        countsByQuality.set(quality, (countsByQuality.get(quality) ?? 0) + 1);
    }

    const knownOrder = [...Object.keys(QUALITY_COLORS), MISSING_QUALITY];
    const orderedQualities = [
        ...knownOrder.filter(q => countsByQuality.has(q)),
        ...Array.from(countsByQuality.keys()).filter(q => !knownOrder.includes(q)).sort(),
    ];

    return {
        qualities: orderedQualities,
        counts: orderedQualities.map(q => countsByQuality.get(q) ?? 0),
        colors: orderedQualities.map(qualityColor),
    };
}

export interface QualitySeries {
    quality: string;
    color: string;
    counts: number[];
}

export interface QualityByCenterData {
    centers: string[];
    series: QualitySeries[];
}

/**
 * Compute the number of dataset acquisitions per quality tag, broken down by center.
 */
export function computeQualityByCenter(rows: StudyStatisticsDTO[]): QualityByCenterData {
    const centerTotals = new Map<number, { name: string, total: number }>();
    const cellCounts = new Map<string, number>();
    const qualities = new Set<string>();

    for (const acquisition of qualityByAcquisition(rows).values()) {
        if (acquisition.centerId == null) continue;
        qualities.add(acquisition.quality);

        const key = acquisition.centerId + '|' + acquisition.quality;
        cellCounts.set(key, (cellCounts.get(key) ?? 0) + 1);

        let centerEntry = centerTotals.get(acquisition.centerId);
        if (!centerEntry) {
            centerEntry = { name: acquisition.centerName, total: 0 };
            centerTotals.set(acquisition.centerId, centerEntry);
        }
        centerEntry.total++;
    }

    const sortedCenters = Array.from(centerTotals.entries()).sort((a, b) => b[1].total - a[1].total);
    const centers = sortedCenters.map(([, entry]) => entry.name);
    const centerIds = sortedCenters.map(([id]) => id);

    const knownOrder = [...Object.keys(QUALITY_COLORS), MISSING_QUALITY];
    const orderedQualities = [
        ...knownOrder.filter(q => qualities.has(q)),
        ...Array.from(qualities).filter(q => !knownOrder.includes(q)).sort(),
    ];

    const series: QualitySeries[] = orderedQualities.map(quality => ({
        quality,
        color: qualityColor(quality),
        counts: centerIds.map(centerId => cellCounts.get(centerId + '|' + quality) ?? 0),
    }));

    return { centers, series };
}

/**
 * importDate is missing for data imported before the field was introduced (january 2022),
 * in which case examinationDate is used as a fallback.
 */
export function parseEffectiveDate(row: StudyStatisticsDTO): Date | null {
    const raw = row.importDate ?? row.examinationDate;
    if (!raw) return null;
    const date = new Date(raw);
    return isNaN(date.getTime()) ? null : date;
}

/**
 * The [min, max] span of effective dates (importDate, falling back to examinationDate)
 * across all rows, used to bound the Parameters date-range slider. Rows with neither
 * date are ignored, as they cannot be placed on the timeline.
 */
export function computeEffectiveDateRange(rows: StudyStatisticsDTO[]): { min: Date, max: Date } | null {
    let min: Date | null = null;
    let max: Date | null = null;
    for (const row of rows) {
        const date = parseEffectiveDate(row);
        if (!date) continue;
        if (!min || date < min) min = date;
        if (!max || date > max) max = date;
    }
    return min && max ? { min, max } : null;
}

/**
 * Restricts rows to those whose effective date (importDate, falling back to
 * examinationDate) falls within [start, end]. Rows with no parseable date are
 * excluded, since their membership in the range cannot be determined.
 */
export function filterRowsByDateRange(rows: StudyStatisticsDTO[], start: Date, end: Date): StudyStatisticsDTO[] {
    return rows.filter(row => {
        const date = parseEffectiveDate(row);
        if (!date) return false;
        return date >= start && date <= end;
    });
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
 * Compute cumulative count of distinct examinations and distinct subjects, by month, based on
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

export interface LatestImportRow {
    commonName: string;
    centerName: string;
    examinationComment: string;
    examinationDate: Date | null;
    importDate: Date | null;
}

/**
 * Compute lastly imported examinations, keeping their most recent import date, 
 * sorted with the most recently imported examination first.
 */
export function computeLatestImports(rows: StudyStatisticsDTO[]): LatestImportRow[] {
    const latestByExam = new Map<number, { row: StudyStatisticsDTO, date: Date }>();
    for (const row of rows) {
        if (row.examinationId == null) continue;
        const date = parseEffectiveDate(row);
        if (!date) continue;
        const existing = latestByExam.get(row.examinationId);
        if (!existing || date > existing.date) {
            latestByExam.set(row.examinationId, { row, date });
        }
    }

    return Array.from(latestByExam.values())
        .sort((a, b) => b.date.getTime() - a.date.getTime())
        .map(({ row }) => ({
            commonName: row.commonName,
            centerName: row.centerName,
            examinationComment: row.examinationComment,
            examinationDate: row.examinationDate ? new Date(row.examinationDate) : null,
            importDate: row.importDate ? new Date(row.importDate) : null,
        }));
}

// One column per StudyStatisticsDTO field, in the order the field is declared in the DTO.
const CSV_COLUMNS: (keyof StudyStatisticsDTO)[] = [
    'studyId', 'centerId', 'centerName', 'centerPrefix', 'subjectId', 'commonName',
    'examinationId', 'examinationComment', 'examinationDate', 'datasetAcquisitionId',
    'importDate', 'datasetId', 'datasetName', 'modality', 'quality',
];

function csvEscape(value: unknown): string {
    if (value == null) return '';
    const str = String(value);
    return /[",\n]/.test(str) ? '"' + str.replace(/"/g, '""') + '"' : str;
}

/**
 * "Export as CSV" button: a raw dump of the (already
 * center/date-range-filtered) statistics rows, one line per dataset, same columns as
 * getStudyStatistics().
 */
export function buildStatisticsCsv(rows: StudyStatisticsDTO[]): string {
    const lines = [
        CSV_COLUMNS.join(','),
        ...rows.map(row => CSV_COLUMNS.map(col => csvEscape(row[col])).join(',')),
    ];
    return lines.join('\n');
}
