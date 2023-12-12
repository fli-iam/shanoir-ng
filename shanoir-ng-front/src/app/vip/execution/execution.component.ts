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
import {ExecutionMonitoring} from 'src/app/vip/models/execution-monitoring.model';
import {Execution} from 'src/app/vip/models/execution';
import {ParameterType} from 'src/app/vip/models/parameterType';
import {Pipeline} from 'src/app/vip/models/pipeline';
import {VipClientService} from 'src/app/vip/shared/vip-client.service';
import {ExecutionMonitoringService} from 'src/app/vip/shared/execution-monitoring.service';
import {Dataset} from 'src/app/datasets/shared/dataset.model';
import {DatasetService} from 'src/app/datasets/shared/dataset.service';
import {DatasetProcessingType} from 'src/app/enum/dataset-processing-type.enum';
import {ColumnDefinition} from 'src/app/shared/components/table/column.definition.type';
import {KeycloakService} from 'src/app/shared/keycloak/keycloak.service';
import {MsgBoxService} from 'src/app/shared/msg-box/msg-box.service';
import {ExecutionDataService} from '../execution.data-service';
import {Option} from '../../shared/select/select.component';
import { formatDate } from '@angular/common';
import {ParameterResourcesDto} from "../models/parameter-resources.dto";
import {GroupByEnum} from "../models/groupby.enum";
import {PipelineParameter} from "../models/pipelineParameter";
import {ServiceLocator} from "../../utils/locator.service";
import {ConsoleService} from "../../shared/console/console.service";

@Component({
    selector: 'app-execution',
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {

    protected consoleService = ServiceLocator.injector.get(ConsoleService);
    pipeline: Pipeline;
    executionForm: UntypedFormGroup;
    selectedDatasets: Set<Dataset>;

    datasetsOptions: Option<Dataset>[];
    token: String;
    refreshToken: String;
    parametersApplied: boolean = false;
    execution: Execution;
    columnDefs: { [key: string]: ColumnDefinition[] } = {};
    datasetsByParam: { [key: string]: Dataset[] } = {};
    fileInputs = [];
    inputDatasets: Set<Dataset>;
    execDefaultName= "";
    exportFormat="nii";
    groupBy = "dataset";
    isLoading = true;
    isSubmitted = true;
    datasetsPromise: Promise<void>;

    constructor(private breadcrumbsService: BreadcrumbsService, private processingService: ExecutionDataService, private vipClientService: VipClientService, private router: Router, private msgService: MsgBoxService, private keycloakService: KeycloakService, private datasetService: DatasetService, private executionMonitoringService: ExecutionMonitoringService) {
        this.breadcrumbsService.nameStep('2. Executions');
        this.selectedDatasets = new Set<Dataset>();
        this.isSubmitted = false;
    }

    ngOnInit(): void {
        if (!this.processingService.selectedPipeline) {
            this.router.navigate(["/pipelines"])
        }

        this.pipeline = this.processingService.selectedPipeline;
        this.initExecutionForm();

        this.datasetsPromise = this.datasetService.getByIds(this.processingService.selectedDatasets).then(
            result => {
                this.selectedDatasets = new Set(result);
                this.createColumnDefs();
                this.isLoading = false;
            });

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

        this.parametersApplied = true;


        this.datasetsPromise.then(() => {

            let availableDatasets: Dataset[] = Array.from(this.selectedDatasets);
            let excludedDatasetsCount = 0;

            this.datasetsOptions = [];
            availableDatasets.forEach(dataset => {
                this.datasetsOptions.push(new Option<Dataset>(dataset, dataset.name + '(' + dataset.id + ')'));
            });

            // By default, we order by alphabtical order
            // TODO: Propose another possible order (by ID?)
            availableDatasets.sort((a: Dataset, b: Dataset) => {
                return a.name.localeCompare(b.name);
            })

            this.pipeline.parameters.forEach(
                parameter => {
                    if (this.isAFile(parameter)) {
                        // If we have a file, we try to set up the adapted dataset
                        // We try to find all adapted datasets
                        let exp = this.executionForm.get(parameter.name).value?.toString() ? this.executionForm.get(parameter.name).value.toString()  : ".*";
                        let nameFilter: RegExp = new RegExp(exp);

                        let paramDatasets: Dataset[] = [];

                        availableDatasets.forEach(dataset => {
                            if(dataset.datasetProcessing){
                                excludedDatasetsCount++;
                            } else if (nameFilter.test(dataset.name)) {
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
            if(excludedDatasetsCount > 0){
                this.consoleService.log('warn', "[" + excludedDatasetsCount + "] processed datasets has been excluded from the selection.");
            }
        });
    }

    async onSubmitExecutionForm() {

        this.isSubmitted = true;

        let processingInit = this.initProcessing();

        this.executionMonitoringService.create(processingInit).then(
            (processing) => {

                let execution = this.initExecution(processing);
                this.setExecutionParameters(processing, execution);

                this.vipClientService.createExecution(execution).then(
                    (execution) => {

                        processing.identifier = execution.identifier;
                        processing.status = execution.status;
                        processing.startDate = execution.startDate;
                        processing.endDate = execution.endDate;

                        this.executionMonitoringService.updateAndStart(processing).then(() => {
                                this.router.navigate([`/dataset-processing/details/${processing.id}`]);
                            },
                            (error) => {
                                this.msgService.log('error', 'Sorry, an error occurred while updating dataset processing.');
                                console.error(error);
                            });
                    },
                    (error) => {
                        this.msgService.log('error', 'Sorry, an error occurred while creating the execution on VIP.');
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

    private initExecution(processing: ExecutionMonitoring) {
        let execution = new Execution();
        execution.name = processing.name;
        execution.pipelineIdentifier = processing.pipelineIdentifier;
        execution.timeout = processing.timeout;
        execution.inputValues = {};
        execution.resultsLocation = this.getResultUri(processing.resultsLocation);
        return execution;
    }

    private setExecutionParameters(processing: ExecutionMonitoring, execution: Execution) {
        processing.parametersResources.forEach(dto => {
            execution.inputValues[dto.parameter] = [];
            let extension = ".nii.gz"
            if (this.exportFormat == "dcm") {
                extension = ".zip"
            }

            dto.resourceIds.forEach(id => {
                let entity_name = `resource_id+${id}+${this.groupBy}${extension}`
                // datasetId URI param = resourceId (to be changed once VIP has been updated)
                let inputValue = `shanoir:/${entity_name}?format=${this.exportFormat}&datasetId=${id}&token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
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
        let processingInit = new ExecutionMonitoring();
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
                    let dto = new ParameterResourcesDto();
                    dto.parameter = parameter.name;
                    dto.groupBy = this.getGroupByEnumByLabel(this.groupBy);
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
