import { IdNameObject } from "../../shared/models/id-name-object.model";
import { MembersCategory } from "./members-category.model";
import { StudyCard } from "../../study-cards/shared/study-card.model";
import { StudyCenter } from "./study-center.model";
import { StudyStatus } from "./study-status.enum";
import { StudyType } from "./study-type.enum";
import { SubjectStudy } from "../../subjects/shared/subject-study.model";
import { Timepoint } from "./timepoint.model";

export class Study {
    centers: IdNameObject[];
    clinical: boolean;
    compatible: boolean;
    downloadableByDefault: boolean;
    endDate: Date;
    experimentalGroupsOfSubjects: IdNameObject[];
    id: number;
    membersCategories: MembersCategory[];
    monoCenter: boolean;
    name: string;
    nbExaminations: number;
    nbSujects: number;
    protocolFilePathList: string[];
    startDate: Date;
    studyCards: StudyCard[];
    studyCenterList: StudyCenter[];
    studyStatus: StudyStatus;
    studyType: StudyType;
    subjects: SubjectStudy[];
    timepoints: Timepoint[];
    visibleByDefault: boolean;
    withExamination: boolean;

    constructor(study?: IdNameObject) {
        if (study) {
            this.id = study.id;
            this.name = study.name;
        }
    }
}