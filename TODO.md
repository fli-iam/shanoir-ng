# Current development notes

## Processed dataset

There is a cycle dependency problem between Dataset and DatasetProcessing (Dataset has a list of processings and DatasetProcessing has a list of input datasets and another of output datasets).

The two last commits try different approach of solving the issue, without success:

commit c8411972e02f7d40aadacbfb33a186a6a9e6aa3c (HEAD -> processed-datasets)
Author: Arthur <arthur.sw@gmail.com>
Date:   Wed Mar 24 17:33:09 2021 +0100

    fixing cyclic dependency

commit d89074cbc2b9072c133ff76273c3121a9e529c95
Author: Arthur <arthur.sw@gmail.com>
Date:   Wed Mar 24 15:58:42 2021 +0100

    fixing cycle dependency

More precisely:

c8411972e02f7d40aadacbfb33a186a6a9e6aa3c: created a DatasetProcessingDTO with inputDatasetIds and outputDatasetIds (list of ids instead of Datasets)

that implied adding one entrypoint to the backend (DatasetProcessingApi) to get the lists of datasets from a dataset processing id:
getOutputDatasets = /{datasetProcessingId}/outputDatasets/

and changing the front with the following class and modified services accordingly:

export class DatasetProcessingDTO {

    id: number;
    comment: string;
    datasetProcessingType: DatasetProcessingType;
    inputDatasetIds: number[];
    outputDatasetIds: number[];
	processingDate: Date;
    studyId: number;

    constructor(datasetProcessing: DatasetProcessing) {
        this.id = datasetProcessing.id;
        this.comment = datasetProcessing.comment;
        this.datasetProcessingType = datasetProcessing.datasetProcessingType;
        this.inputDatasetIds = datasetProcessing.inputDatasets.map((dataset)=> dataset.id);
        this.outputDatasetIds = datasetProcessing.outputDatasets.map((dataset)=> dataset.id);
        this.processingDate = datasetProcessing.processingDate;
        this.studyId = datasetProcessing.studyId;
    }
}

This does not solve the problem because DatasetDTO still has a list of DatasetProcessing (and not DatasetProcessingDTO) which contains lists of datasets (as opposed to DatasetProcessingDTO which contain lists of dataset ids).

d89074cbc2b9072c133ff76273c3121a9e529c95: (made before the above commit) changed the front Dataset model so that it has a list of processing ids (instead of processings). When I saw the implications on the backend, I chose to try another solution (because the backend to convert Dataset to/from DatasetDTO with DatasetProcessings is obscure)

Conclusion: I think we must try understanding and improving the  Dataset to/from DatasetDTO mecanism on the backend.
