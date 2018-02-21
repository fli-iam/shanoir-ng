import { ExaminationDatasetAcquisition } from "../../dataset-acquisitions/shared/examination-dataset-acquisition.model";

export class ExaminationDTO {
    id: number;
    centerId: number;
    centerName: string;
    comment: string;
    note: string;
    studyName: string;
    subjectWeight:number;

}