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

import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { DatasetService, Format } from 'src/app/datasets/shared/dataset.service';

@Injectable()
export class MassDownloadService {

    constructor(private datasetService: DatasetService) {}

    test() { // TODO : DELETE
        this.download([232, 231, 224, 222, 221, 216, 213], 'dcm');
    }

    download(datasetIds: number[], format: Format, nbQueues: number = 4) {
        this.getFolderHandle().then(parentFolderHandle => { // ask the user's parent directory
            let ids = [...datasetIds]; // copy array
            let report = {requestedDatasetIds: datasetIds};
            for (let queueIndex = 0; queueIndex < nbQueues; queueIndex ++) { // build the dl queues
                this.recursiveSave(ids.shift(), format, parentFolderHandle, ids, report);
            }
        }).catch(error => { /* the user clicked 'cancel' in the choose directory window */ });
    }

    private recursiveSave(id: number, format: Format, userFolderHandle: FileSystemDirectoryHandle, remainingIds: number [], report: any): Promise<void> {
        return this.saveDataset(id, format, userFolderHandle, report).then(() => {
            if (remainingIds.length > 0) {
                return this.recursiveSave(remainingIds.shift(), format, userFolderHandle, remainingIds, report);
            } else {
                return Promise.resolve();
            }
        });
    } 

    private saveDataset(id: number, format: Format, userFolderHandle: FileSystemDirectoryHandle, report: any): Promise<void> {
        const metadataPromise: Promise<Dataset> = this.datasetService.get(id, 'lazy');
        const downloadPromise: Promise<HttpResponse<Blob>> = this.datasetService.downloadToBlob(id, format);

        return Promise.all([metadataPromise, downloadPromise]).then(([dataset, httpResponse]) => {
            const blob: Blob = httpResponse.body;
            const filename: string = this.getFilename(httpResponse);
            const path: string = 
                    dataset.datasetAcquisition?.examination?.comment 
                    + '_' + dataset.datasetAcquisition?.examination?.id
                    + '/'
                    + filename
            return this.writeMyFile(path, blob, userFolderHandle);
        }).catch(reason => {

        });
    }

    private writeMyFile(path: string, content: any, userFolderHandle: FileSystemDirectoryHandle): Promise<void> {
        path = path.trim();
        if (path.startsWith('/')) path = path.substring(1); // remove 1st '/'
        let splited: string[] = path.split('/');
        const filename = splited.pop(); // separate filename from dir path

        return this.createDirectoriesIn(splited, userFolderHandle).then(lastFolderHandle => { // create the sub directories
            lastFolderHandle.getFileHandle(filename, { create: true } // create the file handle
                ).then(fileHandler => {
                    this.writeFile(fileHandler, content); // write the file
                });
        });
    }

    private async getFolderHandle(): Promise<FileSystemDirectoryHandle> {
        const options = {
            mode: 'readwrite'
        };
        const handle: FileSystemDirectoryHandle = await window.showDirectoryPicker(options);
        return handle;
    }

    private async writeFile(fileHandle: FileSystemFileHandle, contents) {
        // Create a FileSystemWritableFileStream to write to.
        const writable: FileSystemWritableFileStream = await fileHandle.createWritable();
        // Write the contents of the file to the stream.
        await writable.write(contents);
        // Close the file and write the contents to disk.
        await writable.close();
      }
      
    private createDirectoriesIn(dirs: string[], parentFolderHandle: FileSystemDirectoryHandle): Promise<FileSystemDirectoryHandle> {
        const dirToCreate: string = dirs.shift(); // separate the first element
        return parentFolderHandle.getDirectoryHandle(dirToCreate, { create: true})
                .then(handle => {
                    if (dirs.length > 0) {
                        return this.createDirectoriesIn(dirs, handle);
                    } else return handle;
                });
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length).replace('/', '_');
    }
}

