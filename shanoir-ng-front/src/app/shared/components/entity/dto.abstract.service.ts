// /**
//  * Shanoir NG - Import, manage and share neuroimaging data
//  * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
//  * Contact us on https://project.inria.fr/shanoir/
//  * 
//  * This program is free software: you can redistribute it and/or modify
//  * it under the terms of the GNU General Public License as published by
//  * the Free Software Foundation, either version 3 of the License, or
//  * (at your option) any later version.
//  * 
//  * You should have received a copy of the GNU General Public License
//  * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
//  */

// import { Entity } from './entity.abstract';

// export abstract class AbstractDTOService<T extends Entity, U> {

//     constructor(
//         private entityConstructor: { new(): T },
//     ) {}

//     abstract mapSyncFields(dto: U, entity: T): T;

//     abstract mapAsyncFields(dto: U, entity: T): Promise<T>;

//     /**
//      * Convert from DTO to Entity
//      * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
//      * @param result can be used to get an immediate temporary result without waiting async data
//      */
//     public toEntity(dto: U, result?: T): Promise<T> {        
//         if (!result) result = new this.entityConstructor();
//         this.mapSyncFields(dto, result);
//         return Promise.all([
//             this.studyService.get(dto.studyId).then(study => result.study = study),
//             this.acqEqService.get(dto.acquisitionEquipmentId).then(acqEq => result.acquisitionEquipment = acqEq),
//             this.niftiService.get(dto.niftiConverterId).then(nifti => result.niftiConverter = nifti)
//         ]).then(([]) => {
//             return result;
//         });
//     }

//     private dk(child: Entity) {
//         child.load().then(ret => ? = ret),
//     }

//     /**
//      * Convert from a DTO list to an Entity list
//      * @param result can be used to get an immediate temporary result without waiting async data
//      */
//     public toEntityList(dtos: U[], result?: T[]): Promise<T[]>{
//         if (!result) result = [];
//         for (let dto of dtos) {
//             let entity = new StudyCard();
//             StudyCardDTOService.mapSyncFields(dto, entity);
//             result.push(entity);
//         }
//         return Promise.all([
//             this.studyService.getStudiesNames().then(studies => {
//                 for (let entity of result) {
//                     if (entity.study) 
//                         entity.study.name = studies.find(study => study.id == entity.study.id).name;
//                 }
//             }),
//             this.acqEqService.getAll().then(acqs => {
//                 for (let entity of result) {
//                     if (entity.acquisitionEquipment) 
//                         entity.acquisitionEquipment = acqs.find(acq => acq.id == entity.acquisitionEquipment.id);
//                 }
//             }),
//             this.niftiService.getAll().then(niftis => {
//                 for (let entity of result) {
//                     if (entity.niftiConverter) 
//                         entity.niftiConverter = niftis.find(nifti => nifti.id == entity.niftiConverter.id);
//                 }
//             })
//         ]).then(() => {
//             return result;
//         })
//     }
// }