import { Center } from "../../centers/shared/center.model";
import { IdNameObject } from "../../shared/models/id-name-object.model";
import { MembersCategory } from "./members-category.model";
import { StudyCard } from "./study-card.model";
import { StudyStatus } from "./study-status.enum";
import { StudyType } from "./study-type.enum";
import { SubjectStudy } from "../../subjects/shared/subject-study.model";

export class Study {
    centers: Center[];
	clinical: boolean;
    downloadableByDefault: boolean;
    endDate: Date;
    examinationIds: number[];
    experimentalGroupsOfSubjects: IdNameObject[];
    id: number;
    membersCategories: MembersCategory[];
    monoCenter: boolean;
    name: string;
    nbSujects: number;
    protocolFilePathList: string[];
    startDate: Date;
    studyCards: StudyCard[];
    studyStatus: StudyStatus;
    studyType: StudyType;
    subjects: SubjectStudy[];
    visibleByDefault: boolean;
    withExamination: boolean;
}