import {GroupByEnum} from "./groupby.enum";

export class ParameterResourcesDto {

    parameter: string;
    resourceIds : string[];
    groupBy: GroupByEnum;
    datasetIds : number[];

}
