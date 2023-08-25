import {Component, Input, OnInit} from '@angular/core';
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
import {CarminDatasetProcessing} from 'src/app/carmin/models/carmin-dataset-processing.model';
import {Execution} from 'src/app/carmin/models/execution';
import {ParameterType} from 'src/app/carmin/models/parameterType';
import {Pipeline} from 'src/app/carmin/models/pipeline';
import {CarminClientService} from 'src/app/carmin/shared/carmin-client.service';
import {CarminDatasetProcessingService} from 'src/app/carmin/shared/carmin-dataset-processing.service';
import {Dataset} from 'src/app/datasets/shared/dataset.model';
import {DatasetService} from 'src/app/datasets/shared/dataset.service';
import {DatasetProcessingType} from 'src/app/enum/dataset-processing-type.enum';
import {ColumnDefinition} from 'src/app/shared/components/table/column.definition.type';
import {KeycloakService} from 'src/app/shared/keycloak/keycloak.service';
import {MsgBoxService} from 'src/app/shared/msg-box/msg-box.service';
import {ProcessingService} from '../processing.service';
import {Option} from '../../shared/select/select.component';
import { formatDate } from '@angular/common';
import {DatasetAcquisition} from "../../dataset-acquisitions/shared/dataset-acquisition.model";
import {FileEntity} from "./file-entity";
import {Examination} from "../../examinations/shared/examination.model";
import {ParameterResourcesDTO} from "../../carmin/models/parameter-resources-d-t.o";
import {GroupByEnum} from "../../carmin/models/groupby.enum";
import {PipelineParameter} from "../../carmin/models/pipelineParameter";

@Component({
    selector: 'app-execution',
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {

    pipeline: Pipeline;
    executionForm: UntypedFormGroup;
    selectedDatasets: Set<Dataset>;

    datasetsOptions: Option<Dataset>[];
    token: String;
    refreshToken: String;
    parametersApplied: boolean = false;
    nbExecutions = 0;
    execution: Execution;
    columnDefs: { [key: string]: ColumnDefinition[] } = {};
    datasetsByParam: { [key: string]: Dataset[] } = {};
    tables = [];
    fileInputs = [];
    inputDatasets: Set<Dataset>;
    execDefaultName= "";
    exportFormat="nii";
    groupBy = "dataset";
    selectedGroupBy = "dataset";

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

    addDatasetFromParam(event, paramName) {
        this.datasetsByParam[paramName].push(event);
    }

    removeDatasetFromParam(event, paramName) {
        this.datasetsByParam[paramName].splice(this.datasetsByParam[paramName].indexOf(event), 1);
    }

    initExecutionForm() {
        this.executionForm = new UntypedFormGroup({
            "execution_name": new UntypedFormControl('', Validators.required),
            "export_format": new UntypedFormControl('', Validators.required),
            "group_by": new UntypedFormControl('', Validators.required)
        });

        this.pipeline.parameters.forEach(
            parameter => {
                if (this.isAFile(parameter)) {
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

        this.datasetsOptions = [];
        availableDatasets.forEach(dataset => {
            this.datasetsOptions.push(new Option<Dataset>(dataset, dataset.name + '(' + dataset.id + ')'));
        });

        // By default, we order by alphabtical order
        // TODO: Propose another possible order (by ID?)
        availableDatasets.sort((a: Dataset, b: Dataset) => {
            return a.name.localeCompare(b.name);
        })

        this.selectedGroupBy = this.groupBy;

        this.pipeline.parameters.forEach(
            parameter => {
                if (this.isAFile(parameter)) {
                    // If we have a file, we try to set up the adapted dataset
                    // We try to find all adapted datasets
                    let value = this.executionForm.get(parameter.name).value ? this.executionForm.get(parameter.name).value  : "";
                    let nameFilter: RegExp = new RegExp(value);

                    let paramDatasets: Dataset[] = [];

                    availableDatasets.forEach(dataset => {
                        if (nameFilter.test(dataset.name)) {
                            paramDatasets.push(dataset);
                        }
                    });

                    paramDatasets.forEach(dataset => {
                        availableDatasets.splice(availableDatasets.indexOf(dataset), 1);
                    });

                    this.datasetsByParam[parameter.name] = paramDatasets;

                }
            }
        )
        this.parametersApplied = true;
    }

    async onSubmitExecutionForm() {

        let processingInit = this.initProcessing();

        this.carminDatasetProcessing.create(processingInit).then(
            (processing) => {

                let execution = this.initExecution(processing);
                this.setExecutionParameters(processing, execution);

                this.carminClientService.createExecution(execution).then(
                    (execution) => {

                        processing.identifier = execution.identifier;
                        processing.status = execution.status;
                        processing.startDate = execution.startDate;
                        processing.endDate = execution.endDate;

                        this.carminDatasetProcessing.updateAndStart(processing).then(() => {
                                this.router.navigate([`/dataset-processing/details/${processing.id}`]);
                            },
                            (error) => {
                                this.msgService.log('error', 'Sorry, an error occurred while updating dataset processing.');
                                console.error(error);
                            });
                    },
                    (error) => {
                        this.msgService.log('error', 'Sorry, an error occurred while starting the execution.');
                        console.error(error);
                    }
                )
            },
            (error) => {
                this.msgService.log('error', 'Sorry, an error occurred while creating dataset processing.');
                console.error(error);
            }
        )
    }

    private initExecution(processing: CarminDatasetProcessing) {
        let execution = new Execution();
        execution.name = processing.name;
        execution.pipelineIdentifier = processing.pipelineIdentifier;
        execution.timeout = processing.timeout;
        execution.inputValues = {};
        execution.resultsLocation = this.getResultUri(processing.resultsLocation);
        return execution;
    }

    private setExecutionParameters(processing: CarminDatasetProcessing, execution: Execution) {
        processing.parametersResources.forEach(dto => {
            execution.inputValues[dto.parameter] = [];
            let extension = ".nii.gz"
            if (this.exportFormat == "dcm") {
                extension = ".zip"
            }

            dto.resourceIds.forEach(id => {
                let entity_name = `resource_id+${id}+${this.selectedGroupBy}${extension}`
                let inputValue = `shanoir:/${entity_name}?format=${this.exportFormat}&resourceId=${id}&token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
                execution.inputValues[dto.parameter].push(inputValue);
            })
        });

        this.pipeline.parameters.forEach(
            parameter => {
                if (!this.isAFile(parameter)) {
                    execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value;
                }
            }
        )
    }

    private initProcessing() {
        let processingInit = new CarminDatasetProcessing();
        processingInit.name = this.cleanProcessingName(this.executionForm.get("execution_name").value);
        processingInit.pipelineIdentifier = this.pipeline.identifier
        processingInit.resultsLocation = this.getResultPath();
        processingInit.timeout = 20;
        processingInit.comment = processingInit.name;
        processingInit.studyId = [...this.selectedDatasets][0].study.id;  // TODO : this should be selected automatically if all datasets have the same study, if not show a select input to choose what context.
        processingInit.datasetProcessingType = DatasetProcessingType.SEGMENTATION; // TODO : this should be selected by the user.
        processingInit.outputProcessing = this.pipeline.outputProcessing;
        this.inputDatasets = new Set();
        this.pipeline.parameters.forEach(
            parameter => {
                if (this.isAFile(parameter)) {
                    this.datasetsByParam[parameter.name].forEach(ds => {
                        this.inputDatasets.add(ds);
                    })
                }
            }
        )
        this.inputDatasets.forEach(dataset => {
            dataset.study.subjectStudyList = [];
            dataset.study.studyCenterList = [];
            dataset.subject.subjectStudyList = [];
        })
        processingInit.inputDatasets = Array.from(this.inputDatasets);
        processingInit.parametersResources = [];

        this.pipeline.parameters.forEach(
            parameter => {
                if (this.isAFile(parameter)) {
                    let dto = new ParameterResourcesDTO();
                    dto.parameter = parameter.name;
                    dto.groupBy = this.getGroupByEnumByLabel(this.selectedGroupBy);
                    dto.datasetIds = this.datasetsByParam[parameter.name].map(dataset => { return dataset.id});
                    processingInit.parametersResources.push(dto);
                }
            }
        );

        return processingInit;
    }

    getGroupByEnumByLabel(label: string){
        switch (label){
            case 'dataset' :
                return GroupByEnum.DATASET;
            case 'acquisition' :
                return GroupByEnum.ACQUISITION;
            case 'examination' :
                return GroupByEnum.EXAMINATION;
            case 'study' :
                return GroupByEnum.STUDY;
            case 'subject' :
                return GroupByEnum.SUBJECT;
        }
    }

    isAFile(parameter: PipelineParameter): boolean {
        return parameter.type == ParameterType.File;

    }

    private getResultPath(){
        return `${this.keycloakService.getUserId()}/${Date.now()}`;
    }

    private getResultUri(resultPath: string) {
        return `shanoir:/${resultPath}?token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
    }

    getDefaultExecutionName(): string {
        return this.cleanProcessingName(this.pipeline.name
        + "_" + this.pipeline.version
        + "_" + formatDate(new Date(), 'dd-MM-YYYY_HHmmss', 'en-US'));

    }

    private cleanProcessingName(name: string): string {
        return name.replace(/[^0-9A-Za-z_-]/g, '_')
    }
}
