import { StudyCard } from "./studycard.model";
import { StudyStatus } from "./study-status.enum";
import { StudyType } from "./study-type.enum";

export class Study {
	clinical: boolean;
    downloadableByDefault: boolean;
    endDate: Date;
    examinationIds: number[];
    id: number;
    monoCenter: boolean;
    name: string;
    protocolFilePathList: string[];
    startDate: Date;
    studyCards: StudyCard[];
    studyStatus: StudyStatus;
    studyType: StudyType;
    subjectNames: string[];
    visibleByDefault: boolean;
    withExamination: boolean;
}