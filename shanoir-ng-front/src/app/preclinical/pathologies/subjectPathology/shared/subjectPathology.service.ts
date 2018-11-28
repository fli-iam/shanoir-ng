import { Injectable } from '@angular/core';
import { EntityService } from '../../../../shared/components/entity/entity.abstract.service';
import { SubjectPathology } from './subjectPathology.model';
import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';

@Injectable()
export class SubjectPathologyService extends EntityService<SubjectPathology>{

    API_URL = PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL;

    getEntityInstance() { return new SubjectPathology(); }

    
    getSubjectPathologies(preclinicalSubject: PreclinicalSubject): Promise<SubjectPathology[]> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.get<SubjectPathology[]>(url)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))    
            .toPromise();
    }
    
    
    getSubjectPathology(preclinicalSubject: PreclinicalSubject, pid: string): Promise<SubjectPathology>{
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${pid}`;
        return this.http.get<SubjectPathology>(url)
            .map((entity) => this.toRealObject(entity))
            .toPromise();
    }
  


    updateSubjectPathology(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Promise<SubjectPathology> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${subjectPathology.id}`;
        return this.http
            .put<SubjectPathology>(url, JSON.stringify(subjectPathology))
            .map((entity) => this.toRealObject(entity))
            .toPromise();
    }

    createSubjectPathology(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Promise<SubjectPathology> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}`;
        return this.http
            .post<SubjectPathology>(url, JSON.stringify(subjectPathology))
            .map((entity) => this.toRealObject(entity))
            .toPromise();
    }

    deleteSubjectPathology(preclinicalSubject: PreclinicalSubject, subjectPathology: SubjectPathology): Promise<any> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${preclinicalSubject.animalSubject.id}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${subjectPathology.id}`;
        return this.http.delete<void>(url)
            .toPromise();
    }
    
    deleteAllPathologiesForAnimalSubject(animalSubjectId: number): Promise<any> {
        const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}/${animalSubjectId}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}${PreclinicalUtils.PRECLINICAL_ALL_URL}`;
        return this.http.delete(url)
            .map(res => res)
            .toPromise();
    }

    getAllSubjectForPathologyModel(pid: number): Promise<SubjectPathology[]> {
    	const url = `${PreclinicalUtils.PRECLINICAL_API_SUBJECTS_URL}${PreclinicalUtils.PRECLINICAL_ALL_URL}/${PreclinicalUtils.PRECLINICAL_PATHOLOGY}/${pid}`;
    	return this.http.get<SubjectPathology[]>(url)
            .map(entities => entities.map((entity) => this.toRealObject(entity)))            
            .toPromise();
    }

}