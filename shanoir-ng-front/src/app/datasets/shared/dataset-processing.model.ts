import { DatasetProcessingType } from "../../shared/enums/dataset-processing-type";
import { Dataset } from "./dataset.model";

export class DatasetProcessing {

    private id: number;
    private comment: string;
    private type: DatasetProcessingType;
    private inputDatasets: Array<Dataset>;
    private outputDatasets: Array<Dataset>;
	private processingDate: Date;
    private studyId: number;
    
    public toString(): string {
        return this.type + ' n° ' + this.id; 
    }
}