import {GroupByEnum} from "./groupby.enum";

export class DatasetParameterDTO {

    name: string;
    groupBy: GroupByEnum;
    exportFormat: string;
    datasetIds : number[];

}
