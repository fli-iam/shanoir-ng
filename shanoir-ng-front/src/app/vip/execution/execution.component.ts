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
import {ExecutionService} from 'src/app/vip/execution/execution.service';
import {ExecutionMonitoringService} from 'src/app/vip/execution-monitorings/execution-monitoring.service';
import {Dataset} from 'src/app/datasets/shared/dataset.model';
import {DatasetService} from 'src/app/datasets/shared/dataset.service';
import {DatasetProcessingType} from 'src/app/enum/dataset-processing-type.enum';
import {ColumnDefinition} from 'src/app/shared/components/table/column.definition.type';
import {KeycloakService} from 'src/app/shared/keycloak/keycloak.service';
import {MsgBoxService} from 'src/app/shared/msg-box/msg-box.service';
import {ExecutionDataService} from '../execution.data-service';
import {Option} from '../../shared/select/select.component';
import { formatDate } from '@angular/common';
import {DatasetParameterDTO} from "../models/dataset-parameter.dto";
import {GroupByEnum} from "../models/groupby.enum";
import {PipelineParameter} from "../models/pipelineParameter";
import {ServiceLocator} from "../../utils/locator.service";
import {ConsoleService} from "../../shared/console/console.service";
import {ExecutionCandidateDto} from "../models/execution-candidate.dto";

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
    token: string;
    refreshToken: string;
    parametersApplied: boolean = false;
    execution: Execution;
    columnDefs: { [key: string]: ColumnDefinition[] } = {};
    datasetsByParam: { [key: string]: Dataset[] } = {};
    fileInputs = [];
    execDefaultName= "";
    exportFormat= "nii";
    groupBy = "dataset";
    isLoading = true;
    isSubmitted = true;
    datasetsPromise: Promise<void>;
    converterId: number;

    niftiConverters: Option<number>[] = [
        new Option<number>(1, 'DCM2NII_2008_03_31', null, null, null, false),
        new Option<number>(2, 'MCVERTER_2_0_7', null, null, null, false),
        new Option<number>(4, 'DCM2NII_2014_08_04', null, null, null, false),
        new Option<number>(5, 'MCVERTER_2_1_0', null, null, null, false),
        new Option<number>(6, 'DCM2NIIX', null, null, null, false),
        new Option<number>(7, 'DICOMIFIER', null, null, null, false),
        new Option<number>(8, 'MRICONVERTER', null, null, null, false),
    ];

    constructor(
        private breadcrumbsService: BreadcrumbsService,
        private processingService: ExecutionDataService,
        private executionService: ExecutionService,
        private router: Router,
        private msgService: MsgBoxService,
        private keycloakService: KeycloakService,
        private datasetService: DatasetService) {
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
            (token: string) => {
                this.token = token;
            }
        )
        this.keycloakService.getRefreshToken().then(
            (refreshToken: string) => {
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
            "group_by": new UntypedFormControl('', Validators.required),
            "converter": new UntypedFormControl('')
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
                            paramDatasets.push(dataset);
                        });

                        paramDatasets.forEach(dataset => {
                            availableDatasets.splice(availableDatasets.indexOf(dataset), 1);
                        });

                        this.datasetsByParam[parameter.name] = paramDatasets;

                    }
                }
            )
        });
    }

    async onSubmitExecutionForm() {

        this.isSubmitted = true;

        let exec = this.initExecutionCandidate();
        this.executionService.createExecution(exec).then(
            monitoring => {
                this.router.navigate([`/dataset-processing/details/${monitoring.id}`]);
            },
            (error) => {
                this.msgService.log('error', 'Sorry, an error occurred while submitting execution.');
                console.error(error);
            }
        );
    }

    private initExecutionCandidate() {
        let candidate = new ExecutionCandidateDto();
        candidate.name = this.cleanProcessingName(this.executionForm.get("execution_name").value);
        candidate.pipelineIdentifier = this.pipeline.identifier
        candidate.studyIdentifier = [...this.selectedDatasets][0].study.id;  // TODO : this should be selected automatically if all datasets have the same study, if not show a select input to choose what context.
        candidate.processingType = DatasetProcessingType.SEGMENTATION; // TODO : this should be selected by the user.
        candidate.outputProcessing = this.pipeline.outputProcessing;
        candidate.client = KeycloakService.clientId;
        candidate.refreshToken = this.refreshToken;
        candidate.converterId = this.converterId;
        candidate.datasetParameters = [];
        candidate.inputParameters = {};
        this.pipeline.parameters.forEach(
            parameter => {
                if (this.isAFile(parameter)) {
                    // File type parameters (i.e. datasets)
                    let dto = new DatasetParameterDTO();
                    dto.name = parameter.name;
                    dto.groupBy = this.getGroupByEnumByLabel(this.groupBy);
                    dto.exportFormat = this.exportFormat;
                    dto.datasetIds = this.datasetsByParam[parameter.name].map(dataset => { return dataset.id});
                    candidate.datasetParameters.push(dto);
                }else if (this.executionForm.get(parameter.name).value?.toString()) {
                    candidate.inputParameters[parameter.name] = [ this.executionForm.get(parameter.name).value.toString() ];
                }
            }
        )
        return candidate;
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

    getDefaultExecutionName(): string {
        return this.cleanProcessingName(this.pipeline.name
        + "_" + this.pipeline.version
        + "_" + formatDate(new Date(), 'dd-MM-YYYY_HHmmss', 'en-US'));

    }

    private cleanProcessingName(name: string): string {
        return name.replace(/[^0-9A-Za-z_-]/g, '_')
    }
}
