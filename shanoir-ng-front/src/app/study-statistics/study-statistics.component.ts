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
import { DonutChartComponent } from '../shared/components/donut-chart/donut-chart.component';
import { LineChartComponent, LineChartDataset } from '../shared/components/line-chart/line-chart.component';
import { StudyService } from '../studies/shared/study.service';

import {
    computeDatasetsByModality,
    computeExamsByCenter,
    computeGlobalStatistics,
    computeInclusionsEvolution,
    computeLatestImports,
    computeModalityByCenter,
    computeQualityByCenter,
    computeQualityDistribution,
    computeSubjectsByCenter,
    GlobalStatistics,
    LatestImportRow
} from './study-statistics.utils';

@Component({
    selector: 'study-statistics',
    templateUrl: 'study-statistics.component.html',
    styleUrls: ['study-statistics.component.css'],
    imports: [LineChartComponent, BarChartComponent, DonutChartComponent, DecimalPipe, LocalDateFormatPipe, FormsModule]
})
export class StudyStatisticsComponent implements OnChanges {

    @Input() studyId: number;

    loading: boolean = false;
    error: boolean = false;
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
            const rows = await this.studyService.getStudyStatistics(this.studyId);
            this.globalStats = computeGlobalStatistics(rows);

            const evolution = computeInclusionsEvolution(rows);
            this.labels = evolution.labels;
            this.datasets = [
                // colors match the palette from the home page
                { label: 'Examinations', data: evolution.examinations, color: '#5f0f4e' },
                { label: 'Subjects', data: evolution.subjects, color: '#67aeca' },
            ];

            const examsByCenter = computeExamsByCenter(rows);
            this.examsByCenterLabels = examsByCenter.centers;
            this.examsByCenterDatasets = [
                { label: 'Examinations', data: examsByCenter.counts, color: '#5f0f4e' },
            ];

            const subjectsByCenter = computeSubjectsByCenter(rows);
            this.subjectsByCenterLabels = subjectsByCenter.centers;
            this.subjectsByCenterDatasets = [
                { label: 'Subjects', data: subjectsByCenter.counts, color: '#67aeca' },
            ];

            const datasetsByModality = computeDatasetsByModality(rows);
            this.modalityLabels = datasetsByModality.modalities;
            this.modalityData = datasetsByModality.counts;
            this.modalityColors = datasetsByModality.colors;

            const modalityByCenter = computeModalityByCenter(rows);
            this.modalityByCenterLabels = modalityByCenter.centers;
            this.modalityByCenterDatasets = modalityByCenter.series.map(series => ({
                label: series.modality,
                data: series.counts,
                color: series.color,
            }));

            const qualityDistribution = computeQualityDistribution(rows);
            this.qualityLabels = qualityDistribution.qualities;
            this.qualityData = qualityDistribution.counts;
            this.qualityColors = qualityDistribution.colors;

            const qualityByCenter = computeQualityByCenter(rows);
            this.qualityByCenterLabels = qualityByCenter.centers;
            this.qualityByCenterDatasets = qualityByCenter.series.map(series => ({
                label: series.quality,
                data: series.counts,
                color: series.color,
            }));

            this.latestImports = computeLatestImports(rows);
        } catch {
            this.error = true;
        } finally {
            this.loading = false;
        }
    }

    get displayedLatestImports(): LatestImportRow[] {
        return this.latestImports.slice(0, this.latestImportsPageSize);
    }
}
