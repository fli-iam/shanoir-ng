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
import { Injectable } from '@angular/core';

import { DicomTag } from './study-card.model';
import { HttpClient } from '@angular/common/http';
import { BACKEND_API_STUDY_CARD_URL } from '../../utils/app.utils';


@Injectable()
export class DicomService {

    private tagRequested: boolean = false;
    private tagPromiseResolve: (value?: DicomTag[] | PromiseLike<DicomTag[]>) => void;
    private tagPromise: Promise<DicomTag[]> = new Promise((resolve, reject) => this.tagPromiseResolve = resolve);

    constructor(private http: HttpClient) {}

    getDicomTags(): Promise<DicomTag[]> {
        if (!this.tagRequested) {
            this.tagRequested = true;
            this.http.get<DicomTag[]>(BACKEND_API_STUDY_CARD_URL + '/dicomTags')
                .toPromise()
                .then(tags => {
                    this.tagPromiseResolve(tags);
                });
        }
        return this.tagPromise;
    }
}