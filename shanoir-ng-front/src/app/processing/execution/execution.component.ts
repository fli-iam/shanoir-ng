import {Component, OnInit} from '@angular/core';
import {
    AbstractControl,
    UntypedFormControl,
    UntypedFormGroup,
    FormGroup,
    FormControl,
    ValidatorFn,
    Validators
} from '@angular/forms';
import {Router} from '@angular/router';
import {BreadcrumbsService} from 'src/app/breadcrumbs/breadcrumbs.service';
import {CarminDatasetProcessing} from 'src/app/carmin/models/CarminDatasetProcessing';
import {Execution} from 'src/app/carmin/models/execution';
import {ParameterType} from 'src/app/carmin/models/parameterType';
import {Pipeline} from 'src/app/carmin/models/pipeline';
import {CarminClientService} from 'src/app/carmin/shared/carmin-client.service';
import {CarminDatasetProcessingService} from 'src/app/carmin/shared/carmin-dataset-processing.service';
import {Dataset} from 'src/app/datasets/shared/dataset.model';
import {DatasetService} from 'src/app/datasets/shared/dataset.service';
import {DatasetProcessingType} from 'src/app/enum/dataset-processing-type.enum';
import {ColumnDefinition} from 'src/app/shared/components/table/column.definition.type';
import {Page} from 'src/app/shared/components/table/pageable.model';
import {KeycloakService} from 'src/app/shared/keycloak/keycloak.service';
import {MsgBoxService} from 'src/app/shared/msg-box/msg-box.service';
import {ProcessingService} from '../processing.service';
import {Option} from '../../shared/select/select.component';
import { formatDate } from '@angular/common';

@Component({
    selector: 'app-execution',
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {

    pipeline: Pipeline;
    executionForm: UntypedFormGroup;
    selectedDatasets: Set<Dataset>;
    datasetOptions: Option<Dataset>[];
    token: String;
    refreshToken: String;
    parametersApplied: boolean = false;
    nbExecutions = 0;
    execution: Execution;
    columnDefs: { [key: string]: ColumnDefinition[] } = {};
    datasets: { [key: string]: Dataset[] } = {};
    tables = [];
    fileInputs = [];
    inputDatasets: Dataset[] = [];
    execDefaultName= "";

    constructor(private breadcrumbsService: BreadcrumbsService, private processingService: ProcessingService, private carminClientService: CarminClientService, private router: Router, private msgService: MsgBoxService, private keycloakService: KeycloakService, private datasetService: DatasetService, private carminDatasetProcessing: CarminDatasetProcessingService) {
        this.breadcrumbsService.nameStep('2. Executions');
        this.selectedDatasets = new Set<Dataset>();
    }

    ngOnInit(): void {
        if (!this.processingService.isAnyPipelineSelected()) {
            this.router.navigate(["/processing/pipelines"])
        }

        this.processingService.selectedPipeline.subscribe(
            (pipeline: Pipeline) => {
                this.pipeline = pipeline;
                this.initExecutionForm();
                this.processingService.selectedDatasets.subscribe(
                    (datasets: Set<number>) => {
                        this.datasetService.getByIds(datasets).then(
                            result => {
                                this.selectedDatasets = new Set(result);
                                this.datasetOptions = [];
                                this.selectedDatasets.forEach(dataset => {
                                    this.datasetOptions.push(new Option<Dataset>(dataset, dataset.name + '(' + dataset.id + ')'));
                                });
                                this.createColumnDefs();
                            });
                    });
            }
        )
        this.keycloakService.getToken().then(
            (token: String) => {
                this.token = token;
            }
        )
        this.keycloakService.getRefreshToken().then(
            (refreshToken: String) => {
                this.refreshToken = refreshToken;
            }
        )
        this.execDefaultName = this.getDefaultExecutionName();
    }

    addDataset(event, paramName) {
        this.datasets[paramName].push(event);
    }

    removeDataset(dataset, paramName) {
        this.datasets[paramName].splice(this.datasets[paramName].indexOf(dataset), 1);
    }

    initExecutionForm() {
        this.executionForm = new UntypedFormGroup({
            "execution_name": new UntypedFormControl('', Validators.required)
        });

        this.pipeline.parameters.forEach(
            parameter => {
                if (parameter.type == ParameterType.File) {
                    this.fileInputs.push(parameter);
                }
                let validators: ValidatorFn[] = [];
                if (!parameter.isOptional && parameter.type != ParameterType.Boolean && parameter.type != ParameterType.File) {
                    validators.push(Validators.required);
                }
                let control = new UntypedFormControl(parameter.defaultValue, validators);
                if (parameter.name != "executable") {
                    this.executionForm.addControl(parameter.name, control);
                }
            }
        )
    }

    private createColumnDefs() {
        this.pipeline.parameters.forEach(parameter => {
            if (parameter.type == ParameterType.File) {
                this.columnDefs[parameter.name] = [];
                this.columnDefs[parameter.name].push({headerName: parameter.name, field: "name"});
            }
        });
    }

    // Here we create a bunch of executions with default parameters
    onApplyParameters() {
        this.parametersApplied = false;

        let availableDatasets: Dataset[] = Array.from(this.selectedDatasets);

        // By default, we order by alphabtical order
        // TODO: Propose another possible order (by ID?)
        availableDatasets.sort((a: Dataset, b: Dataset) => {
            return a.name.localeCompare(b.name);
        })

        let execution: Execution = new Execution();

        execution.name = this.executionForm.get("execution_name").value;
        execution.pipelineIdentifier = this.pipeline.identifier;
        execution.timeout = 20;
        execution.inputValues = {};

        this.pipeline.parameters.forEach(
            parameter => {
                if (parameter.type == ParameterType.File) {
                    // If we have a file, we try to set up the adapted dataset
                    // We try to find all adapted datasets
                    let datasetFilter: RegExp = new RegExp(this.executionForm.get(parameter.name).value);

                    let datasetsToSet: Dataset[] = [];

                    availableDatasets.forEach(dataset => {
                        if (datasetFilter.test(dataset.name)) {
                            datasetsToSet.push(dataset);
                        }
                    });

                    datasetsToSet.forEach(dataset => {
                        availableDatasets.splice(availableDatasets.indexOf(dataset), 1);
                    })

                    this.datasets[parameter.name] = datasetsToSet;

                    // TODO the format should be selected depending on the pipeline.
                    // File ad md5 values should be selected automcatically depending on the pipeline.
                    execution.inputValues[parameter.name] = this.getDatasetsValue(datasetsToSet);
                } else if (parameter.type == ParameterType.Boolean) {
                    execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value ? true : false;
                } else {
                    execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value;
                }
            }
        )
        this.parametersApplied = true;
    }

    getDatasetsValue(datasets) {
        // TODO the dataset extension format should be selected depending on the pipeline.
        return datasets.forEach(dataset => {
            let dataset_name = `id+${dataset.id}+${dataset.name.replace(/ /g, "_")}.nii.gz`
            return `shanoir:/${dataset_name}?format=nii&datasetId=${dataset.id}&token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
        })
    }

    getPage(parameterName: string, pageable: any): Promise<Page<Dataset>> {
        let page: Page<Dataset> = new Page();
        page.content = Array.from(this.datasets[parameterName]);
        page.number = 1;
        page.size = this.datasets[parameterName].length;
        page.numberOfElements = this.datasets[parameterName].length;
        page.totalElements = this.datasets[parameterName].length;
        page.totalPages = Math.ceil(page.numberOfElements / page.size);
        return new Promise((resolve, reject) => {
            resolve(page);
        });
    }

    onSubmitExecutionForm() {
        let execution: Execution = new Execution();

        execution.name = this.executionForm.get("execution_name").value;
        execution.pipelineIdentifier = this.pipeline.identifier;
        execution.timeout = 20;
        execution.inputValues = {};

        this.pipeline.parameters.forEach(
            parameter => {
                if (parameter.type == ParameterType.File) {
                    execution.inputValues[parameter.name] = [];
                    let datasetsOf = this.datasets[parameter.name];
                    datasetsOf.forEach(dataset => {
                        // TODO the dataset extension format should be selected depending on the pipeline.
                        let dataset_name = `id+${dataset.id}+${dataset.name.replace(/ /g, "_")}.nii.gz`

                        // TODO the format should be selected depending on the pipeline.
                        // File ad md5 values should be selected automcatically depending on the pipeline.
                        execution.inputValues[parameter.name].push(`shanoir:/${dataset_name}?format=nii&datasetId=${dataset.id}&token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`);
                        this.inputDatasets.push(dataset);
                    })
                } else {
                    execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value;
                }
            }
        )

        /**
         * Init result location
         * The result directory should be dynamic
         */
        let resultPath = this.generateResultPath();
        execution.resultsLocation = `shanoir:/${resultPath}?token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
        this.carminClientService.createExecution(execution).then(
            (execution: Execution) => {
                this.msgService.log('info', 'the execution successfully started.')

                let carminDatasetProcessing: CarminDatasetProcessing = new CarminDatasetProcessing(execution.identifier, execution.name, execution.pipelineIdentifier, resultPath, execution.status, execution.timeout, execution.startDate, execution.endDate);

                carminDatasetProcessing.comment = execution.identifier;
                carminDatasetProcessing.studyId = [...this.selectedDatasets][0].study.id;  // TODO : this should be selected automatically if all datasets have the same study, if not show a select input to choose what context.
                carminDatasetProcessing.datasetProcessingType = DatasetProcessingType.SEGMENTATION; // TODO : this should be selected by the user.
                carminDatasetProcessing.outputProcessing = this.pipeline.outputProcessing;

                // HOTFIX for circular dataset object issue
                this.inputDatasets.forEach(dataset => {
                    dataset.study.subjectStudyList = [];
                    dataset.study.studyCenterList = [];
                    dataset.subject.subjectStudyList = [];
                })

                carminDatasetProcessing.inputDatasets = Array.from(this.inputDatasets);

                this.carminDatasetProcessing.create(carminDatasetProcessing).then(
                    (response: CarminDatasetProcessing) => {
                        this.router.navigate([`/dataset-processing/details/${response.id}`]);
                    },
                    (error) => {
                        this.msgService.log('error', 'Sorry, an error occurred while creating dataset processing.');
                        console.error(error);
                    }
                )
            },
            (error) => {
                this.msgService.log('error', 'Sorry, an error occurred while starting the execution.');
                console.error(error);
            }
        )
    }

    getParameterType(parameterType: ParameterType): String {
        switch (parameterType) {
            case ParameterType.String:
            case ParameterType.Boolean:
                return 'text';
            case ParameterType.Int64:
            case ParameterType.Double:
                return 'number';
            case ParameterType.File:
                return 'file';
        }
    }

    isAFile(parameterType: ParameterType): boolean {
        if (parameterType == ParameterType.File) return true;
        return false;
    }

    private generateResultPath() {
        return this.keycloakService.getUserId() + "/" + Date.now();
    }

    getDefaultExecutionName(): string {
        return this.pipeline.name
        + "_" + this.pipeline.version
        + "_" + formatDate(new Date(), 'dd-MM-YYYY_HH:mm:ss', 'en-US');

    }
}
