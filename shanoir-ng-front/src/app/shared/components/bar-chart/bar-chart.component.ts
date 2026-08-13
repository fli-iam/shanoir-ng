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
    // A single color for the whole series, or one color per bar
    color?: string | string[];
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
    // Draws the bars horizontally (categories on the y axis) for many/long category labels.
    @Input() horizontal: boolean = true;
    // Stacks multiple datasets into a single bar per category instead of grouping them side by side.
    @Input() stacked: boolean = false;

    chartData: ChartConfiguration<'bar'>['data'];
    chartOptions: ChartOptions<'bar'>;

    ngOnChanges(): void {
        this.chartData = {
            labels: this.labels,
            datasets: this.datasets.map(ds => ({
                label: ds.label,
                data: ds.data,
                backgroundColor: ds.color,
                // caps bar thickness so a chart with very few categories
                // doesn't render a couple of oversized bars filling the whole chart
                maxBarThickness: 40,
            })),
        };
        const valueAxis = this.horizontal ? 'x' : 'y';
        const categoryAxis = this.horizontal ? 'y' : 'x';
        this.chartOptions = {
            indexAxis: this.horizontal ? 'y' : 'x',
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                // counts are always whole numbers - not decimal values
                [valueAxis]: { beginAtZero: true, ticks: { precision: 0 }, stacked: this.stacked },
                [categoryAxis]: { stacked: this.stacked },
            },
            plugins: {
                legend: { display: this.datasets.length > 1 },
            },
        };
    }
}
