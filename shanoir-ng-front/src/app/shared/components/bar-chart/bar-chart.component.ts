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

import { Component, Input, OnChanges } from '@angular/core';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

export interface BarChartDataset {
    label: string;
    data: number[];
    color?: string;
}

@Component({
    selector: 'shanoir-bar-chart',
    templateUrl: 'bar-chart.component.html',
    styleUrls: ['bar-chart.component.css'],
    imports: [BaseChartDirective]
})
export class BarChartComponent implements OnChanges {

    @Input() labels: string[] = [];
    @Input() datasets: BarChartDataset[] = [];
    @Input() loading: boolean = false;
    @Input() emptyMessage: string = 'No data available';
    /** Draws the bars horizontally (categories on the y axis) - the default, most legible layout for many/long category labels. */
    @Input() horizontal: boolean = true;

    chartData: ChartConfiguration<'bar'>['data'];
    chartOptions: ChartOptions<'bar'>;

    ngOnChanges(): void {
        this.chartData = {
            labels: this.labels,
            datasets: this.datasets.map(ds => ({
                label: ds.label,
                data: ds.data,
                backgroundColor: ds.color,
            })),
        };
        this.chartOptions = {
            indexAxis: this.horizontal ? 'y' : 'x',
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                x: { beginAtZero: true },
            },
            plugins: {
                legend: { display: this.datasets.length > 1 },
            },
        };
    }
}
