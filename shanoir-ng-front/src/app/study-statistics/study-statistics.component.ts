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

import { DecimalPipe } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { LocalDateFormatPipe } from '../shared/localLanguage/localDateFormat.pipe';
import { BarChartComponent, BarChartDataset } from '../shared/components/bar-chart/bar-chart.component';
import { DateRange, DateRangeSliderComponent } from '../shared/components/date-range-slider/date-range-slider.component';
import { DonutChartComponent } from '../shared/components/donut-chart/donut-chart.component';
import { LineChartComponent, LineChartDataset } from '../shared/components/line-chart/line-chart.component';
import { StudyStatisticsDTO } from '../studies/shared/study.dto';
import { StudyService } from '../studies/shared/study.service';

import {
    centerHighlightColors,
    computeCenterNames,
    computeDatasetsByModality,
    computeEffectiveDateRange,
    computeExamsByCenter,
    computeGlobalStatistics,
    computeInclusionsEvolution,
    computeLatestImports,
    computeModalityByCenter,
    computeQualityByCenter,
    computeQualityDistribution,
    computeSubjectsByCenter,
    filterRowsByDateRange,
    GlobalStatistics,
    LatestImportRow
} from './study-statistics.utils';

@Component({
    selector: 'study-statistics',
    templateUrl: 'study-statistics.component.html',
    styleUrls: ['study-statistics.component.css'],
    imports: [LineChartComponent, BarChartComponent, DonutChartComponent, DateRangeSliderComponent, DecimalPipe, LocalDateFormatPipe, FormsModule]
})
export class StudyStatisticsComponent implements OnChanges {

    @Input() studyId: number;

    loading: boolean = false;
    error: boolean = false;
    allRows: StudyStatisticsDTO[] = [];
    dateRangeMin?: Date;
    dateRangeMax?: Date;
    filterStartDate?: Date;
    filterEndDate?: Date;
    availableCenters: string[] = [];
    selectedCenter: string | null = null;
    private dateFilteredRows: StudyStatisticsDTO[] = [];
    labels: string[] = [];
    datasets: LineChartDataset[] = [];
    examsByCenterLabels: string[] = [];
    examsByCenterDatasets: BarChartDataset[] = [];
    subjectsByCenterLabels: string[] = [];
    subjectsByCenterDatasets: BarChartDataset[] = [];
    modalityLabels: string[] = [];
    modalityData: number[] = [];
    modalityColors: string[] = [];
    modalityByCenterLabels: string[] = [];
    modalityByCenterDatasets: BarChartDataset[] = [];
    qualityLabels: string[] = [];
    qualityData: number[] = [];
    qualityColors: string[] = [];
    qualityByCenterLabels: string[] = [];
    qualityByCenterDatasets: BarChartDataset[] = [];
    latestImports: LatestImportRow[] = [];
    latestImportsPageSize: number = 10;
    globalStats?: GlobalStatistics;

    constructor(private studyService: StudyService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['studyId'] && this.studyId) {
            this.loadStatistics();
        }
    }

    private async loadStatistics() {
        this.loading = true;
        this.error = false;
        try {
            this.allRows = await this.studyService.getStudyStatistics(this.studyId);
            this.availableCenters = computeCenterNames(this.allRows);
            this.selectedCenter = null;
            const dateRange = computeEffectiveDateRange(this.allRows);
            this.dateRangeMin = dateRange?.min;
            this.dateRangeMax = dateRange?.max;
            this.filterStartDate = dateRange?.min;
            this.filterEndDate = dateRange?.max;
            this.dateFilteredRows = this.allRows;
            this.recompute();
        } catch {
            this.error = true;
        } finally {
            this.loading = false;
        }
    }

    onRangeChange(range: DateRange): void {
        this.filterStartDate = range.start;
        this.filterEndDate = range.end;
        const isFullRange = range.start.getTime() === this.dateRangeMin?.getTime() && range.end.getTime() === this.dateRangeMax?.getTime();
        this.dateFilteredRows = isFullRange ? this.allRows : filterRowsByDateRange(this.allRows, range.start, range.end);
        this.recompute();
    }

    onCenterChange(center: string | null): void {
        this.selectedCenter = center;
        this.recompute();
    }

    private recompute() {
        const rows = this.dateFilteredRows;
        // Every chart except "by center" ones is scoped to the selected center only, like
        // Neurovasc-Dashboard's center filter - the "by center" ones instead keep every
        // center's bar and grey out the ones that aren't selected (see below).
        const centerRows = this.selectedCenter ? rows.filter(row => row.centerName === this.selectedCenter) : rows;

        this.globalStats = computeGlobalStatistics(centerRows);

        const evolution = computeInclusionsEvolution(centerRows);
        this.labels = evolution.labels;
        this.datasets = [
            // colors match the palette from the home page
            { label: 'Examinations', data: evolution.examinations, color: '#5f0f4e' },
            { label: 'Subjects', data: evolution.subjects, color: '#67aeca' },
        ];

        const examsByCenter = computeExamsByCenter(rows);
        this.examsByCenterLabels = examsByCenter.centers;
        this.examsByCenterDatasets = [
            { label: 'Examinations', data: examsByCenter.counts, color: centerHighlightColors(examsByCenter.centers, this.selectedCenter, '#5f0f4e') },
        ];

        const subjectsByCenter = computeSubjectsByCenter(rows);
        this.subjectsByCenterLabels = subjectsByCenter.centers;
        this.subjectsByCenterDatasets = [
            { label: 'Subjects', data: subjectsByCenter.counts, color: centerHighlightColors(subjectsByCenter.centers, this.selectedCenter, '#67aeca') },
        ];

        const datasetsByModality = computeDatasetsByModality(centerRows);
        this.modalityLabels = datasetsByModality.modalities;
        this.modalityData = datasetsByModality.counts;
        this.modalityColors = datasetsByModality.colors;

        const modalityByCenter = computeModalityByCenter(centerRows);
        this.modalityByCenterLabels = modalityByCenter.centers;
        this.modalityByCenterDatasets = modalityByCenter.series.map(series => ({
            label: series.modality,
            data: series.counts,
            color: series.color,
        }));

        const qualityDistribution = computeQualityDistribution(centerRows);
        this.qualityLabels = qualityDistribution.qualities;
        this.qualityData = qualityDistribution.counts;
        this.qualityColors = qualityDistribution.colors;

        const qualityByCenter = computeQualityByCenter(centerRows);
        this.qualityByCenterLabels = qualityByCenter.centers;
        this.qualityByCenterDatasets = qualityByCenter.series.map(series => ({
            label: series.quality,
            data: series.counts,
            color: series.color,
        }));

        this.latestImports = computeLatestImports(centerRows);
    }

    get displayedLatestImports(): LatestImportRow[] {
        return this.latestImports.slice(0, this.latestImportsPageSize);
    }
}
