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

import { Center } from '../../centers/shared/center.model';
import { Id } from '../../shared/models/id.model';
import { StudyCardDTOServiceAbstract } from '../../study-cards/shared/study-card.dto.abstract';
import { StudyCardDTO } from '../../study-cards/shared/study-card.dto.model';
import { StudyCard } from '../../study-cards/shared/study-card.model';
import { SubjectStudyDTO } from '../../subjects/shared/subject-study.dto';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { Subject } from '../../subjects/shared/subject.model';
import { SubjectWithSubjectStudy } from '../../subjects/shared/subject.with.subject-study.model';
import { Tag } from '../../tags/tag.model';
import { StudyCenter, StudyCenterDTO } from './study-center.model';
import { StudyType } from './study-type.enum';
import { StudyUser, StudyUserDTO } from './study-user.model';
import { Study } from './study.model';
import {Profile} from '../../shared/models/profile.model';
import {DatasetExpressionFormat} from "../../enum/dataset-expression-format.enum";

@Injectable()
export class StudyDTOService {

    constructor(
    ) {}

    /**
     * Convert from DTO to Entity
     * Warning : DO NOT USE THIS IN A LOOP, use toEntityList instead
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntity(dto: StudyDTO, result?: Study): Promise<Study> {
        if (!result) result = new Study();
        StudyDTOService.mapSyncFields(dto, result);
        return Promise.resolve(result);
    }

    /**
     * Convert from a DTO list to an Entity list
     * @param result can be used to get an immediate temporary result without waiting async data
     */
    public toEntityList(dtos: StudyDTO[], result?: Study[]): Promise<Study[]>{
        if (!result) result = [];
        if (dtos) {
            for (let dto of dtos) {
                let entity = new Study();
                StudyDTOService.mapSyncFields(dto, entity);
                result.push(entity);
            }
        }
        return Promise.resolve(result);
    }

    public toSubjectWithSubjectStudyList(dtos: SubjectWithSubjectStudyDTO[], result: SubjectWithSubjectStudy[]): Promise<SubjectWithSubjectStudy[]> {
        if (!result) result = [];
        if (dtos) {
            for (let dto of dtos) {
                let entity = new SubjectWithSubjectStudy();
                entity.id = dto.id;
                entity.name = dto.name;
                entity.identifier = dto.identifier;
                entity.birthDate = dto.birthDate ? new Date(dto.birthDate) : null;
                entity.subjectStudy = dto.subjectStudy ? StudyDTOService.dtoToSubjectStudy(dto.subjectStudy) : null;
                result.push(entity);
            }
        }
        return Promise.resolve(result);
    }

    static mapSyncFields(dto: StudyDTO, entity: Study): Study {
        entity.clinical = dto.clinical;
        entity.downloadableByDefault = dto.downloadableByDefault;
        entity.endDate = dto.endDate ? new Date(dto.endDate) : null;
        //entity.experimentalGroupsOfSubjects = dto.experimentalGroupsOfSubjects;
        entity.id = dto.id;
        entity.challenge = dto.challenge;
        entity.name = dto.name;
        entity.nbExaminations = dto.nbExaminations;
        entity.nbSubjects = dto.nbSubjects;
        entity.protocolFilePaths = dto.protocolFilePaths;
        entity.profile = dto.profile;
        entity.description = dto.description;
        entity.license = dto.license;
        entity.dataUserAgreementPaths = dto.dataUserAgreementPaths;
        entity.startDate = dto.startDate ? new Date(dto.startDate) : null;
        if (dto.studyCenterList) {
            entity.studyCenterList = (dto.studyCenterList as StudyCenterDTO[]).map(dtoStudyCenter => {
                let studyCenter: StudyCenter = this.dtoToStudyCenter(dtoStudyCenter);
                studyCenter.study = entity;
                return studyCenter;
            });
        } else {
            entity.studyCenterList = [];
        }
        entity.studyStatus = dto.studyStatus;
        entity.studyType = dto.studyType;
        if (dto.subjectStudyList) {
            entity.subjectStudyList = dto.subjectStudyList.map(subjectStudyDto => this.dtoToSubjectStudy(subjectStudyDto, entity));
        } else {
            entity.subjectStudyList = [];
        }
        if (dto.studyUserList) {
            entity.studyUserList = dto.studyUserList.map(studyUserDto => {
                let studyUser: StudyUser = new StudyUser();
                //studyUser.completeMember = studyUserDto.completeMember;
                studyUser.confirmed = studyUserDto.confirmed;
                studyUser.id = studyUserDto.id;
                studyUser.receiveNewImportReport = studyUserDto.receiveNewImportReport;
                studyUser.receiveStudyUserReport = studyUserDto.receiveStudyUserReport;
                studyUser.study = entity;
                studyUser.studyUserRights = studyUserDto.studyUserRights;
                studyUser.user = studyUserDto.user;
                studyUser.userId = studyUserDto.userId;
                studyUser.userName = studyUserDto.userName;
                studyUser.centers = studyUserDto.centerIds?.map(centerId => {
                    let center: Center = new Center();
                    center.id = centerId;
                    return center;
                });
                return studyUser;
            });
        } else {
            entity.studyUserList = [];
        }
        //entity.timepoints = dto.timepoints;
        entity.visibleByDefault = dto.visibleByDefault;
        entity.withExamination = dto.withExamination;
        entity.studyCardPolicy = dto.studyCardPolicy;
        if (dto.studyUserList) {
            entity.nbMembers = dto.studyUserList.length;
        }
        if (dto.studyCards) {
            entity.studyCardList = dto.studyCards.map(studyCardDTO => StudyCardDTOServiceAbstract.mapSyncFields(studyCardDTO, new StudyCard()));
        } else {
            entity.studyCardList = [];
        }
        if (dto.tags) {
            entity.tags = dto.tags.map(this.tagDTOToTag);
        } else {
            entity.tags = [];
        }

        if (dto.studyTags) {
          entity.studyTags = dto.studyTags.map(this.tagDTOToTag);
        } else {
          entity.studyTags = [];
        }

        if(dto.storageVolume){
            entity.totalSize = dto.storageVolume.total;
            entity.detailedSizes = this.studyStorageVolumeDTOToDetailedSizes(dto.storageVolume)
        }

        return entity;
    }

    static studyStorageVolumeDTOToDetailedSizes(dto: StudyStorageVolumeDTO): Map<String, number> {
        let datasetSizes = dto;
        let sizesByLabel = new Map<String, number>()

        for(let sizeByFormat of datasetSizes.volumeByFormat){
            if(sizeByFormat.size > 0){
                sizesByLabel.set(DatasetExpressionFormat.getLabel(sizeByFormat.format), sizeByFormat.size);
            }
        }

        if(datasetSizes.extraDataSize > 0){
            sizesByLabel.set("Other files (DUA, protocol...)", datasetSizes.extraDataSize);
        }

        return sizesByLabel;
    }

    static tagDTOToTag(tagDTO: any): Tag {
        let tag: Tag = new Tag();
        tag.id = tagDTO.id;
        tag.name = tagDTO.name;
        tag.color = tagDTO.color;
        return tag;
    }

    static dtoToSubjectStudy(subjectStudyDto: SubjectStudyDTO, study?: Study, subject?: Subject): SubjectStudy {
        let subjectStudy: SubjectStudy = new SubjectStudy();
        subjectStudy.id = subjectStudyDto.id;
        subjectStudy.physicallyInvolved = subjectStudyDto.physicallyInvolved;
        if (study) {
            subjectStudy.study = study;
            subjectStudy.studyId = study.id;
            subjectStudy.study.tags = study.tags;
            subjectStudy.study.studyTags = study.studyTags;
        } else if (subjectStudyDto.study) {
            subjectStudy.study = new Study();
            subjectStudy.study.id = subjectStudyDto.study.id;
            subjectStudy.study.name = subjectStudyDto.study.name;
            subjectStudy.study.tags = subjectStudyDto.study.tags ? subjectStudyDto.study.tags.map(this.tagDTOToTag) : [];
            subjectStudy.study.studyTags = subjectStudyDto.study.studyTags ? subjectStudyDto.study.studyTags.map(this.tagDTOToTag) : [];
        }
        subjectStudy.studyId = subjectStudy.study.id;
        if (subject) {
            subjectStudy.subject = subject;
            subjectStudy.subjectId = subject.id;
        } else if (subjectStudyDto.subject) {
            subjectStudy.subject = new Subject();
            subjectStudy.subject.id = subjectStudyDto.subject.id;
            subjectStudy.subjectId = subjectStudyDto.subject.id;
            subjectStudy.subject.name = subjectStudyDto.subject.name;
            subjectStudy.subject.preclinical = subjectStudyDto.subjectPreclinical;
        }
        subjectStudy.subjectStudyIdentifier = subjectStudyDto.subjectStudyIdentifier;
        subjectStudy.subjectType = subjectStudyDto.subjectType;
        if (subjectStudyDto.tags) {
          subjectStudy.tags = subjectStudyDto.tags.map(this.tagDTOToTag);
        } else {
          subjectStudy.tags = [];
        }
        subjectStudy.qualityTag = subjectStudyDto.qualityTag;
        return subjectStudy;
    }

    static dtoToStudyCenter(dtoStudyCenter: StudyCenterDTO): StudyCenter {
        let studyCenter: StudyCenter = new StudyCenter();
        studyCenter.id = dtoStudyCenter.id;
        if (dtoStudyCenter.center) {
            studyCenter.center = new Center();
            studyCenter.center.id = dtoStudyCenter.center.id;
            studyCenter.center.name = dtoStudyCenter.center.name;
            studyCenter.subjectNamePrefix = dtoStudyCenter.subjectNamePrefix;
        }
        return studyCenter;
    }

    static centerStudyDTOtoStudy(dto: CenterStudyDTO): Study {
        let study: Study = new Study();
        study.id = dto.id;
        study.name = dto.name;
        study.profile = dto.profile;
        if (dto.studyCenterList) {
            study.studyCenterList = (dto.studyCenterList as StudyCenterDTO[]).map(dtoStudyCenter => {
                return this.dtoToStudyCenter(dtoStudyCenter);
            });
        } else {
            study.studyCenterList = [];
        }
        if (dto.tags) {
          study.tags = dto.tags.map(this.tagDTOToTag);
        } else {
          study.tags = [];
        }
        return study;
    }
}

export class StudyDTO {
    id: number;
    clinical: boolean;
    downloadableByDefault: boolean;
    endDate: Date;
    //examinationIds: number[];
    experimentalGroupsOfSubjects: Id[];
    challenge: boolean;
    name: string;
    nbExaminations: number;
    nbSubjects: number;
    nbMembers: number;
    protocolFilePaths: string[];
    dataUserAgreementPaths: string[];
    profile: Profile;
    startDate: Date;
    studyCenterList: StudyCenterDTO[];
    studyStatus: 'IN_PROGRESS' | 'FINISHED';
    studyType: StudyType;
    studyUserList: StudyUserDTO[];
    subjectStudyList: SubjectStudyDTO[] = [];
    //timepoints: Timepoint[];
    visibleByDefault: boolean;
    withExamination: boolean;
    studyCardPolicy: string;
    tags: Tag[];
    studyTags: Tag[];
    studyCards: StudyCardDTO[];
    description: string;
    license: string;
    storageVolume: StudyStorageVolumeDTO;

    constructor(study: Study) {
        this.id = study.id ? study.id : null;
        this.clinical = study.clinical;
        this.downloadableByDefault = study.downloadableByDefault;
        this.endDate = study.endDate;
        this.experimentalGroupsOfSubjects = study.experimentalGroupsOfSubjects ? study.experimentalGroupsOfSubjects.map(egos => new Id(egos.id)) : null;
        this.name = study.name;
        this.profile = study.profile;
        this.challenge = study.challenge;
        this.protocolFilePaths = study.protocolFilePaths;
        this.dataUserAgreementPaths = study.dataUserAgreementPaths;
        this.startDate = study.startDate;
        this.studyCenterList = study.studyCenterList ? study.studyCenterList.map(sc => {
            let dto = new StudyCenterDTO(sc);
            dto.study = null;
            return dto;
        }) : null;
        this.studyStatus = study.studyStatus;
        this.studyType = study.studyType;
        this.studyUserList = study.studyUserList ? study.studyUserList.map(su => {
            let dto = new StudyUserDTO(su);
            dto.study = null;
            return dto;
        }) : null;
        this.subjectStudyList = study.subjectStudyList ? study.subjectStudyList.map(ss => {
            let dto = new SubjectStudyDTO(ss);
            dto.study = null;
            return dto;
        }) : null;
        this.visibleByDefault = study.visibleByDefault;
        this.studyCardPolicy = study.studyCardPolicy;
        this.withExamination = study.withExamination;
        this.tags = study.tags;
        this.studyTags = study.studyTags;
        this.description = study.description;
        this.license = study.license;
    }

}

export class SubjectWithSubjectStudyDTO {

    id: number;
    name: string;
    identifier: string;
    subjectStudy: SubjectStudyDTO;
    birthDate: Date;

    constructor(subject: SubjectWithSubjectStudy) {
        this.id = subject.id;
        this.name = subject.name;
        this.identifier = subject.identifier;
        this.subjectStudy = subject.subjectStudy ? new SubjectStudyDTO(subject.subjectStudy) : null;
        this.birthDate = subject.birthDate;
    }
}

export class CenterStudyDTO {

    id: number;
    name: string;
    studyCenterList: StudyCenterDTO[];
    profile: Profile;
    tags: Tag[];
}

export class StudyLight {
  downloadableByDefault: boolean;
  challenge: boolean;
  endDate: Date;
  id: number;
  name: string;
  nbExaminations: number;
  nbSubjects: number;
  startDate: Date;
  studyStatus: "IN_PROGRESS" | "FINISHED";
  studyType: StudyType;
  description: string;
  license: string;
  studyTags: Tag[];
  profile: Profile;
}


export class StudyStorageVolumeDTO {

    total: number;
    volumeByFormat: VolumeByFormatDTO[];
    extraDataSize: number;

}


export class VolumeByFormatDTO {

    format: DatasetExpressionFormat;
    size: number;

}
