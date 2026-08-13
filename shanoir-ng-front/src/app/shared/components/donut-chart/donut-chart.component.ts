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
import { ChartConfiguration, ChartOptions, TooltipItem } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

@Component({
    selector: 'shanoir-donut-chart',
    templateUrl: 'donut-chart.component.html',
    styleUrls: ['donut-chart.component.css'],
    imports: [BaseChartDirective]
})
export class DonutChartComponent implements OnChanges {

    @Input() labels: string[] = [];
    @Input() data: number[] = [];
    @Input() colors: string[] = [];
    @Input() loading: boolean = false;
    @Input() emptyMessage: string = 'No data available';

    chartData: ChartConfiguration<'doughnut'>['data'];
    chartOptions: ChartOptions<'doughnut'>;

    ngOnChanges(): void {
        this.chartData = {
            labels: this.labels,
            datasets: [{
                data: this.data,
                backgroundColor: this.colors,
            }],
        };
        this.chartOptions = {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '50%',
            plugins: {
                legend: { display: true, position: 'right' },
                tooltip: {
                    callbacks: {
                        // shows the slice's share of the total as a percentage, rather than the raw count
                        label: (item: TooltipItem<'doughnut'>) => {
                            const values = item.dataset.data as number[];
                            const total = values.reduce((sum, value) => sum + value, 0);
                            const percentage = total > 0 ? (item.parsed / total) * 100 : 0;
                            return `${item.label}: ${percentage.toFixed(1)}%`;
                        },
                    },
                },
            },
        };
    }
}
