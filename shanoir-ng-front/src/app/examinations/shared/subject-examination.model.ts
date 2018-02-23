import { ExaminationDatasetAcquisition } from "../../dataset-acquisitions/shared/examination-dataset-acquisition.model";

export class SubjectExamination {
    id: number;
    comment: string;
    examinationDate: Date;
    datasetAcquisitions: ExaminationDatasetAcquisition[];
}