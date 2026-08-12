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

import { LineChartComponent, LineChartDataset } from '../../shared/components/charts/line-chart.component';
import { StudyLight, StudyStatisticsDTO } from '../../studies/shared/study.dto';
import { StudyService } from '../../studies/shared/study.service';

import { computeInclusionsEvolution } from './inclusions-evolution.util';

@Component({
    selector: 'statistics-block',
    templateUrl: 'statistics-block.component.html',
    styleUrls: ['statistics-block.component.css'],
    imports: [LineChartComponent]
})
export class StatisticsBlockComponent implements OnChanges {

    @Input() studies: StudyLight[];

    loading: boolean = false;
    labels: string[] = [];
    datasets: LineChartDataset[] = [];
    private loaded: boolean = false;

    constructor(private studyService: StudyService) {
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['studies'] && this.studies && !this.loaded && !this.loading) {
            this.loadStatistics();
        }
    }

    get hasData(): boolean {
        return this.labels.length > 0;
    }

    private async loadStatistics() {
        this.loading = true;
        this.loaded = true;
        try {
            const settled = await Promise.allSettled(
                this.studies.map(study => this.studyService.getStudyStatistics(study.id))
            );
            const rows: StudyStatisticsDTO[] = settled
                .filter((result): result is PromiseFulfilledResult<StudyStatisticsDTO[]> => result.status === 'fulfilled')
                .flatMap(result => result.value);
            settled
                .filter((result): result is PromiseRejectedResult => result.status === 'rejected')
                .forEach(result => console.warn('Failed to load statistics for a study', result.reason));

            const evolution = computeInclusionsEvolution(rows);
            this.labels = evolution.labels;
            this.datasets = [
                { label: 'Examinations', data: evolution.examinations, color: 'blue' },
                { label: 'Subjects', data: evolution.subjects, color: 'darkorange' },
            ];
        } finally {
            this.loading = false;
        }
    }
}
