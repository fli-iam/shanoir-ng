import { ExaminationDatasetAcquisition } from "../../dataset-acquisitions/shared/examination-dataset-acquisition.model";

export class SubjectExamination {
    id: number;
    comment: string;
    datasetAcquisitions: ExaminationDatasetAcquisition[]
    examinationDate: Date;
}