import {GroupByEnum} from "./groupby.enum";

export class ParameterResourcesDTO {

    parameter: string;
    resourceIds : string[];
    groupBy: GroupByEnum;
    datasetIds : number[];

}
