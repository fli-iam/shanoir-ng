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

import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';

import { LineChartComponent, LineChartDataset } from '../shared/components/line-chart/line-chart.component';
import { StudyService } from '../studies/shared/study.service';

import { computeInclusionsEvolution } from './study-statistics.utils';

@Component({
    selector: 'study-statistics',
    templateUrl: 'study-statistics.component.html',
    styleUrls: ['study-statistics.component.css'],
    imports: [LineChartComponent]
})
export class StudyStatisticsComponent implements OnChanges {

    @Input() studyId: number;

    loading: boolean = false;
    error: boolean = false;
    labels: string[] = [];
    datasets: LineChartDataset[] = [];

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
            const evolution = computeInclusionsEvolution(rows);
            this.labels = evolution.labels;
            this.datasets = [
                { label: 'Examinations', data: evolution.examinations, color: 'blue' },
                { label: 'Subjects', data: evolution.subjects, color: 'darkorange' },
            ];
        } catch {
            this.error = true;
        } finally {
            this.loading = false;
        }
    }
}
