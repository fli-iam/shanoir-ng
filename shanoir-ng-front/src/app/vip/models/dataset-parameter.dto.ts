import {GroupByEnum} from "./groupby.enum";

export class DatasetParameterDTO {

    parameter: string;
    groupBy: GroupByEnum;
    exportFormat: string;
    datasetIds : number[];

}
