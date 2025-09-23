import { Injectable } from '@angular/core';

import { Pipeline } from './models/pipeline';

@Injectable({
    providedIn: 'root'
})
export class ExecutionDataService {

    //observables
    public selectedDatasets: Set<number>;
    public selectedPipeline: Pipeline;

    constructor() {
    }

    public setDatasets(datasetsIds: Set<number>) {
        this.selectedDatasets = datasetsIds;
    }

    public setPipeline(pipeline: Pipeline) {
        this.selectedPipeline = pipeline;
    }
}
