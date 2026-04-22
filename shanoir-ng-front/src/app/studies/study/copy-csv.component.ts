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
import { Component, ElementRef, HostBinding, HostListener, inject, Input, ViewChild } from '@angular/core';

import { ConfirmDialogService } from 'src/app/shared/components/confirm-dialog/confirm-dialog.service';

import { CopyData, CopyDataService } from '../shared/copy-data.service';

import { TreeService } from './tree.service';


@Component({
    selector: 'copy-from-csv',
    template: `
        <button class="right-icon" type="button">
            Copy from mapping file
            <i class="fas fa-file-arrow-up"></i>
            <input #input hidden type="file" (change)="copyDatasetsTo($event)" accept=".csv, .tsv"/>
        </button>
    `,
    standalone: false
})

export class CopyFromCsvComponent {
    
    @Input() studyId: any;
    @ViewChild('input') inputEl: ElementRef;
    private copyDataService: CopyDataService = inject(CopyDataService);
    private treeService = inject(TreeService);
    private confirmService = inject(ConfirmDialogService);

    @HostListener('click') onClick() {
        console.log("CopyFromCsvComponent clicked");
        this.inputEl?.nativeElement.click();
    }

    @HostBinding('attr.title') title = ''
        + 'Copy datasets from CSV/TSV file\n'
        + 'Columns needed :\n'
        + '\t- serieId (dataset id)\n'
        + '\t- subjectId\n'
        + '\t- subjectName (new name for the copied subject, OPTIONAL)\n'
        + '\t- centerId';

    protected copyDatasetsTo(event: Event) {
        (event.target as HTMLInputElement).files[0]?.text().then(csv => {
            const rawData: string[][] = this.parseCsvTsv(csv);
            let copyData: CopyData;
            try {
                copyData = this.convertToCopyData(rawData);
            } catch (e) {
                if (e instanceof MissingColumnsError) {
                    this.confirmService.error(
                        'Bad format',
                        'The CSV/TSV file must contain the following columns: serieId, subjectId, centerId, subjectName');
                    return;
                }
            }
            this.copyDataService.copy(copyData).then(() => {
                this.treeService.updateTree();
            });
        });
    }

    private parseCsvTsv(input: string): string[][] {
        const lines = input
            .trim()
            .split(/\r?\n/)
            .filter(l => l.trim().length > 0);

        if (lines.length === 0) return [];
        const firstLine = lines[0];
        const delimiter = firstLine.includes("\t") ? "\t" : ",";
        return lines.map(line =>
            line.split(delimiter).map(c => c.trim())
        );
    }

    private convertToCopyData(rawData: string[][]): CopyData {
        const datasetIdIndex = rawData[0].indexOf("serieId");
        const subjectIdIndex = rawData[0].indexOf("subjectId");
        const newSubjectNameIndex = rawData[0].indexOf("subjectName");
        const centerIdIndex = rawData[0].indexOf("centerId");
        if (datasetIdIndex < 0 || subjectIdIndex < 0 || centerIdIndex < 0) {
            throw new MissingColumnsError("The CSV/TSV file must contain the following columns: serieId, subjectId, centerId");
        }
        const copyData: CopyData = {
            datasets: [],
            subjects: [],
        };
        for (let i = 1; i < rawData.length; i++) {
            const line = rawData[i];
            if (datasetIdIndex >= 0) {
                copyData.datasets.push({
                    datasetId: +line[datasetIdIndex],
                    centerId: +line[centerIdIndex],
                    subjectId: +line[subjectIdIndex]
                });
            }
            if (subjectIdIndex >= 0) {
                if (copyData.subjects.find(s => s.id === +line[subjectIdIndex]) == null) {
                    copyData.subjects.push({
                        id: +line[subjectIdIndex],
                        newName: newSubjectNameIndex >= 0 ? line[newSubjectNameIndex] : undefined
                    });
                }
            }
        }
        copyData.targetStudyId = this.studyId;
        return copyData;
    }
}

export class MissingColumnsError extends Error {
    constructor(message: string) {
        super(message);
        this.name = "MissingColumnsError";
    }
}