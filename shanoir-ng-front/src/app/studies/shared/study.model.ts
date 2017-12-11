import { IdNameObject } from "../../shared/models/id-name-object.model";
import { MembersCategory } from "./members-category.model";
import { StudyCenter } from "./study-center.model";
import { StudyStatus } from "./study-status.enum";
import { StudyType } from "./study-type.enum";
import { SubjectStudy } from "../../subjects/shared/subject-study.model";

export class Study {
    clinical: boolean;
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
    studyCards: IdNameObject[];
    studyCenterList: StudyCenter[];
    studyStatus: StudyStatus;
    studyType: StudyType;
    subjects: SubjectStudy[];
    visibleByDefault: boolean;
    withExamination: boolean;
}