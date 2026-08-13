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

import { Component, Input, OnChanges, ViewChild } from '@angular/core';
import { ChartConfiguration, ChartOptions } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import 'chartjs-plugin-zoom';

export interface LineChartDataset {
    label: string;
    data: number[];
    color?: string;
}

@Component({
    selector: 'shanoir-line-chart',
    templateUrl: 'line-chart.component.html',
    styleUrls: ['line-chart.component.css'],
    imports: [BaseChartDirective]
})
export class LineChartComponent implements OnChanges {

    @Input() labels: string[] = [];
    @Input() datasets: LineChartDataset[] = [];
    @Input() loading: boolean = false;
    @Input() emptyMessage: string = 'No data available';
    @Input() yAxisLabel: string;
    /** Enables mouse-drag range selection (zoom) and panning along the x axis. */
    @Input() zoomEnabled: boolean = false;

    @ViewChild(BaseChartDirective) chartDirective: BaseChartDirective;

    chartData: ChartConfiguration<'line'>['data'];
    chartOptions: ChartOptions<'line'>;

    ngOnChanges(): void {
        this.chartData = {
            labels: this.labels,
            datasets: this.datasets.map(ds => ({
                label: ds.label,
                data: ds.data,
                borderColor: ds.color,
                backgroundColor: ds.color,
                fill: false,
                tension: 0.2,
                pointRadius: 2,
            })),
        };
        this.chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    title: { display: !!this.yAxisLabel, text: this.yAxisLabel },
                },
            },
            plugins: {
                legend: { display: true },
                zoom: this.zoomEnabled ? {
                    pan: { enabled: true, mode: 'x' },
                    zoom: {
                        drag: { enabled: true },
                        mode: 'x',
                    },
                } : undefined,
            },
        };
    }

    resetZoom(): void {
        this.chartDirective?.chart?.resetZoom();
    }
}
