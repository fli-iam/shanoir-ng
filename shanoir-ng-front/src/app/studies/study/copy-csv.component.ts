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
import { Component, ElementRef, HostListener, inject, Input, ViewChild } from '@angular/core';

import { CopyDataService } from '../shared/copy-data.service';

@Component({
    selector: 'copy-from-csv',
    template: `
        Copy from mapping file
        <i class="fas fa-file-arrow-up"></i>
        <input #input hidden type="file" (change)="copyDatasetsTo($event)" accept=".csv"/>
    `,
    standalone: false
})

export class CopyFromCsvComponent {
    
    @Input() studyId: any;
    @ViewChild('input') inputEl: ElementRef;
    private copyDataService: CopyDataService = inject(CopyDataService);

    @HostListener('click') onClick() {
        this.inputEl?.nativeElement.click();
    }

    protected copyDatasetsTo(event: Event) {
        (event.target as HTMLInputElement).files[0].text().then(csv => {
            const rawData: string[][] = this.parseCsv(csv);
            const copyData = this.convertToCopyData(rawData);
            this.copyDataService.copyData(copyData);
        });
    }

    private parseCsv(csv: string): string[][] {
        return csv
            .trim()
            .split(/\r?\n/)
            .filter(l => l.trim().length > 0)
            .map(line => line.split(",").map(c => c.trim()));
    }

    private convertToCopyData(rawData: string[][]): CopyData {
        const datasetIdIndex = rawData[0].indexOf("serieId");
        const subjectIdIndex = rawData[0].indexOf("subjectId");
        const newSubjectNameIndex = rawData[0].indexOf("subjectName");
        const centerIdIndex = rawData[0].indexOf("centerId");
        const copyData: CopyData = {
            datasetIds: [],
            subjects: [],
            centerIds: []
        };
        for (let i = 1; i < rawData.length; i++) {
            const line = rawData[i];
            if (datasetIdIndex >= 0) {
                copyData.datasetIds.push(+line[datasetIdIndex]);
            }
            if (newSubjectNameIndex >= 0) {
                if (copyData.subjects.find(s => s.id === +line[subjectIdIndex]) == null) {
                    copyData.subjects.push({
                        id: +line[subjectIdIndex],
                        newName: line[newSubjectNameIndex]
                    });
                }
            }
            if (centerIdIndex >= 0) {
                const centerId = +line[centerIdIndex];
                if (!copyData.centerIds.includes(centerId)) {
                    copyData.centerIds.push(centerId);
                }
            }
        }
        copyData.targetStudyId = this.studyId;
        return copyData;
    }

}

export interface CopyData {
    datasetIds: number[];
    subjects: {
        id: number;
        newName: string;
    }[];
    centerIds: number[];
    targetStudyId?: number;
}
