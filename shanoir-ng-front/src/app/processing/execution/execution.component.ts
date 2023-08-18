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
import {KeycloakService} from 'src/app/shared/keycloak/keycloak.service';
import {MsgBoxService} from 'src/app/shared/msg-box/msg-box.service';
import {ProcessingService} from '../processing.service';
import {Option} from '../../shared/select/select.component';
import { formatDate } from '@angular/common';
import {DatasetAcquisition} from "../../dataset-acquisitions/shared/dataset-acquisition.model";
import {FileEntity} from "./file-entity";
import {Examination} from "../../examinations/shared/examination.model";

@Component({
    selector: 'app-execution',
    templateUrl: './execution.component.html',
    styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {

    pipeline: Pipeline;
    executionForm: UntypedFormGroup;
    selectedDatasets: Set<Dataset>;

    fileEntitiesOptions: Option<FileEntity>[];
    token: String;
    refreshToken: String;
    parametersApplied: boolean = false;
    nbExecutions = 0;
    execution: Execution;
    columnDefs: { [key: string]: ColumnDefinition[] } = {};
    fileEntities: { [key: string]: FileEntity[] } = {};
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

    addFileEntity(event, paramName) {
        this.fileEntities[paramName].push(event);
    }

    removeFileEntity(event, paramName) {
        this.fileEntities[paramName].splice(this.fileEntities[paramName].indexOf(event), 1);
    }

    initExecutionForm() {
        this.executionForm = new UntypedFormGroup({
            "execution_name": new UntypedFormControl('', Validators.required),
            "export_format": new UntypedFormControl('', Validators.required),
            "group_by": new UntypedFormControl('', Validators.required)
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

        let availableFileEntities: FileEntity[] = this.getFileEntitiesFromDatasets(Array.from(this.selectedDatasets));

        this.fileEntitiesOptions = [];
        availableFileEntities.forEach(fileEntity => {
            this.fileEntitiesOptions.push(new Option<FileEntity>(fileEntity, fileEntity.name + '(' + fileEntity.entity.id + ')'));
        });

        // By default, we order by alphabtical order
        // TODO: Propose another possible order (by ID?)
        availableFileEntities.sort((a: FileEntity, b: FileEntity) => {
            return a.name.localeCompare(b.name);
        })

        let execution: Execution = new Execution();

        execution.name = this.executionForm.get("execution_name").value;
        execution.pipelineIdentifier = this.pipeline.identifier;
        execution.timeout = 20;
        execution.inputValues = {};

        this.selectedGroupBy = this.groupBy;

        this.pipeline.parameters.forEach(
            parameter => {
                if (parameter.type == ParameterType.File) {
                    // If we have a file, we try to set up the adapted dataset
                    // We try to find all adapted datasets
                    let value = this.executionForm.get(parameter.name).value ? this.executionForm.get(parameter.name).value  : "";
                    let nameFilter: RegExp = new RegExp(value);

                    let fileEntitiesToSet: FileEntity[] = [];

                    availableFileEntities.forEach(fileEntity => {
                        if (nameFilter.test(fileEntity.name)) {
                            fileEntitiesToSet.push(fileEntity);
                        }
                    });

                    fileEntitiesToSet.forEach(fileEntity => {
                        availableFileEntities.splice(availableFileEntities.indexOf(fileEntity), 1);
                    });

                    this.fileEntities[parameter.name] = fileEntitiesToSet;

                } else if (parameter.type == ParameterType.Boolean) {
                    execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value ? true : false;
                } else {
                    execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value;
                }
            }
        )
        this.parametersApplied = true;
    }

    getFileEntitiesFromDatasets(datasets: Dataset[]): FileEntity[]{
        let fileEntities = new Map();
        datasets.forEach(ds => {
            if(this.groupBy == 'dataset'){
                fileEntities.set(ds.id, new FileEntity(ds.name, ds.subject.name, ds, this.getFileEntityUri(ds.id, ds.name)));
            }else if (this.groupBy == 'acquisition' && ds.datasetAcquisition) {
                let name = ds.datasetAcquisition.type + " dataset acquisition " + ds.datasetAcquisition.id;
                fileEntities.set(ds.datasetAcquisition.id, new FileEntity(name, ds.subject.name, ds.datasetAcquisition, this.getFileEntityUri(ds.datasetAcquisition.id, name)));
            }else if (this.groupBy == 'examination' && ds.datasetAcquisition && ds.datasetAcquisition.examination) {
                let exam = ds.datasetAcquisition.examination
                fileEntities.set(exam.id, new FileEntity(exam.comment, ds.subject.name, exam, this.getFileEntityUri(exam.id, exam.comment)));
            }
        })
        return Array.from(fileEntities.values());

    }

    getFileEntityUri(id, name) {
        let extension = ".nii.gz"
        if(this.exportFormat == "dcm") {
            extension = ".zip"
        }
        let entity_name = this.groupBy + `_id+${id}+${name.replace(/ /g, "_")}${extension}`
        return `shanoir:/${entity_name}?format=${this.exportFormat}&${this.groupBy}Id=${id}&token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
    }

    // getPage(parameterName: string, pageable: any): Promise<Page<Dataset>> {
    //     let page: Page<Dataset> = new Page();
    //     page.content = Array.from(this.fil[parameterName]);
    //     page.number = 1;
    //     page.size = this.datasets[parameterName].length;
    //     page.numberOfElements = this.datasets[parameterName].length;
    //     page.totalElements = this.datasets[parameterName].length;
    //     page.totalPages = Math.ceil(page.numberOfElements / page.size);
    //     return new Promise((resolve, reject) => {
    //         resolve(page);
    //     });
    // }

    async onSubmitExecutionForm() {
        let execution: Execution = new Execution();

        execution.name = this.cleanExecutionName(this.executionForm.get("execution_name").value);
        execution.pipelineIdentifier = this.pipeline.identifier;
        execution.timeout = 20;
        execution.inputValues = {};

        this.inputDatasets = new Set();

        this.setExecutionParams(execution);

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

                carminDatasetProcessing.comment = execution.name;
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

    private setExecutionParams(execution: Execution) {
        this.pipeline.parameters.forEach(
            parameter => {
                if (parameter.type == ParameterType.File) {
                    execution.inputValues[parameter.name] = [];

                    let fileEntitiesOf = this.fileEntities[parameter.name];
                    fileEntitiesOf.forEach(fileEntity => {
                        // File ad md5 values should be selected automatically depending on the pipeline.
                        execution.inputValues[parameter.name].push(fileEntity.uri);
                        this.getInputDatasets(fileEntity).then(datasets => {
                            datasets.forEach(ds => this.inputDatasets.add(ds))
                        });
                    })
                } else {
                    execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value;
                }
            }
        )
    }

    async getInputDatasets(fileEntity: FileEntity): Promise<Dataset[]>{

        if(fileEntity.entity instanceof Dataset){
            return [fileEntity.entity];
        }else if(fileEntity.entity instanceof DatasetAcquisition){
            return await this.datasetService.getByAcquisitionId(fileEntity.entity.id).then(acqDs => {
                return acqDs;
            });
        }else if(fileEntity.entity instanceof Examination){
            return await this.datasetService.getByExaminationId(fileEntity.entity.id).then(examDs => {
                return examDs;
            });
        }
        return [];

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
        return this.cleanExecutionName(this.pipeline.name
        + "_" + this.pipeline.version
        + "_" + formatDate(new Date(), 'dd-MM-YYYY_HHmmss', 'en-US'));

    }

    private cleanExecutionName(name: string): string {
        return name.replace(/[^0-9A-Za-z_-]/g, '_')
    }
}
