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

import { AnimalSubject } from './animalSubject.model';
import { Subject }    from '../../../subjects/shared/subject.model';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import * as AppUtils from '../../../utils/app.utils';
import { EntityService } from '../../../shared/components/entity/entity.abstract.service';
import { PreclinicalSubject } from './preclinicalSubject.model';

@Injectable()
export class AnimalSubjectService extends EntityService<PreclinicalSubject>{

    API_URL = PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL;

    getEntityInstance() { return new PreclinicalSubject(); }

             
    getAnimalSubjects(): Promise<AnimalSubject[]>{
        return this.http.get<AnimalSubject[]>(PreclinicalUtils.PRECLINICAL_API_SUBJECTS_ALL_URL)
            .toPromise();
    }
    
    getPreclinicalSubjects(preclinical : boolean): Promise<Subject[]> {
        return this.http.get<Subject[]>(AppUtils.BACKEND_API_SUBJECT_FILTER_URL+"/"+preclinical)
            .toPromise();
    }

    getAnimalSubject(id: number): Promise<AnimalSubject>{
        return this.http.get<AnimalSubject>(PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL+"/"+id)
            .toPromise();
    }

    updateAnimalSubject(animalSubject: AnimalSubject): Promise<AnimalSubject> {
      const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/`+animalSubject.id;
      return this.http
        .put<AnimalSubject>(url, JSON.stringify(animalSubject))
        .toPromise();
    }

    createAnimalSubject(animalSubject: AnimalSubject): Promise<AnimalSubject> {
        return this.http.post<AnimalSubject>(PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL, animalSubject.stringify())
            .toPromise();
    }


    findAnimalSubjectBySubjectId(subjectId: number){
        return this.http.get<AnimalSubject>(PreclinicalUtils.PRECLINICAL_API_SUBJECT_FIND_URL+"/"+subjectId)
            .toPromise();
    }
    
    findSubjectByIdentifier(identifier: string): Promise<Subject> {
        return this.http.get<Subject>(AppUtils.BACKEND_API_SUBJECT_FIND_BY_IDENTIFIER + '/' + identifier)
        .toPromise()    ;
    }
}