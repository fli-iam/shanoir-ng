import { ViewChild } from '@angular/core';
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { CarminDatasetProcessing } from 'src/app/carmin/models/CarminDatasetProcessing';
import { Execution } from 'src/app/carmin/models/execution';
import { ParameterType } from 'src/app/carmin/models/parameterType';
import { Pipeline } from 'src/app/carmin/models/pipeline';
import { CarminClientService } from 'src/app/carmin/shared/carmin-client.service';
import { CarminDatasetProcessingService } from 'src/app/carmin/shared/carmin-dataset-processing.service';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { DatasetProcessingType } from 'src/app/enum/dataset-processing-type.enum';
import { ColumnDefinition } from 'src/app/shared/components/table/column.definition.type';
import { Page } from 'src/app/shared/components/table/pageable.model';
import { TableComponent } from 'src/app/shared/components/table/table.component';
import { KeycloakService } from 'src/app/shared/keycloak/keycloak.service';
import { MsgBoxService } from 'src/app/shared/msg-box/msg-box.service';
import { Option } from 'src/app/shared/select/select.component';
import { ProcessingService } from '../processing.service';

@Component({
  selector: 'app-execution',
  templateUrl: './execution.component.html',
  styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {

  pipeline: Pipeline;
  executionForm: FormGroup;
  selectedDatasets: Set<Dataset>;
  token: String;
  refreshToken: String;
  parametersApplied: boolean = false;
  allExecsValid: boolean = false;
  nbExecutions = 0;
  executions: Execution[] = [];
  columnDefs: ColumnDefinition[] = [];
  customActionDefs: any[];

  @ViewChild('executionsTable') table: TableComponent;

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
                    this.recalculateExecutionNumber();
                    this.createColumnDefs();
                    this.getCustomActions();
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
  }

  initExecutionForm() {
    this.executionForm = new FormGroup({
      "execution_name": new FormControl('', Validators.required)
    });

    this.pipeline.parameters.forEach(
      parameter => {
        let validators: ValidatorFn[] = [];
        if (!parameter.isOptional && parameter.type != ParameterType.Boolean) validators.push(Validators.required);
        let control = new FormControl(parameter.defaultValue, validators);
        if (parameter.name != "executable") this.executionForm.addControl(parameter.name, control);
      }
    )
  }
  
  private getCustomActions() {
    this.customActionDefs = [];
    this.customActionDefs.push({title: "New",awesome: "fa-solid fa-plus", action: item => this.addExecution()});
  }

  addExecution() {
    let execution: Execution = new Execution();
    
    execution.name = this.executionForm.get("execution_name").value;
    execution.pipelineIdentifier = this.pipeline.identifier;
    execution.timeout = 20;
    execution.inputValues = {};

    this.pipeline.parameters.forEach(
      parameter => {
        if (parameter.type == ParameterType.File) {
            // If we have a file, we try to set up the adapted dataset
          //let dataset = this.executionForm.get(parameter.name).value;
          // We try to find the first adapted dataset
          let datasetFilter: RegExp = new RegExp(this.executionForm.get(parameter.name).value);
          let selectedDatasets = Array.from(this.selectedDatasets);
          let index = selectedDatasets.findIndex(ds => {
            return datasetFilter.test(ds.name);
          })
          let dataset;
          if (index != -1) {
              // Remove dataset and get it (should be of length one)
              dataset = selectedDatasets.splice(index, 1)[0];              
          } else {
              // If regex is wrong / faulty
              dataset = selectedDatasets.splice(0, 1)[0];
          }

          // TODO the format should be selected depending on the pipeline.
          // File ad md5 values should be selected automcatically depending on the pipeline.
          execution.inputValues[parameter.name] = this.getDatasetValue(dataset);
        } else if (parameter.type == ParameterType.Boolean) {
            execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value ? true : false;
        } else {
          execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value;
        }
      }
    )
    this.executions.push(execution);
    this.table.refresh();
    this.areAllExecutionsValid();
  }

  private createColumnDefs() {
    let fileOptions = [];
    this.columnDefs = [];
    for (let dataset of this.selectedDatasets) {
        fileOptions.push(new Option(this.getDatasetValue(dataset), dataset.id + "_" + dataset.name));
    }

    // width as percentage
    let width = 1 / (this.pipeline.parameters.length + 1);
    
    this.columnDefs.push({ headerName: "Execution name", field: "name", editable: true, width: width * 100 + "%", onEdit: () => this.areAllExecutionsValid()});

    this.pipeline.parameters.forEach( parameter => {
        if (parameter.type == ParameterType.File) {
            this.columnDefs.push({ headerName: parameter.name, field: "inputValues." + parameter.name, editable: true, width: width * 100 + "%", onEdit: () =>this.areAllExecutionsValid(), possibleValues: fileOptions});
        } else if (parameter.type == ParameterType.Boolean) {
            this.columnDefs.push({ headerName: parameter.name, field: "inputValues." + parameter.name, editable: true, width: width * 100 + "%", onEdit: () =>this.areAllExecutionsValid(), type :"boolean"});
        } else if (parameter.type == ParameterType.Int64 || parameter.type == ParameterType.Double) {
            this.columnDefs.push({ headerName: parameter.name, field: "inputValues." + parameter.name, editable: true, width: width * 100 + "%", onEdit: () =>this.areAllExecutionsValid(), type :"number" });
        } else {
            this.columnDefs.push({ headerName: parameter.name, field: "inputValues." + parameter.name, editable: true, width: width * 100 + "%", onEdit: () =>this.areAllExecutionsValid(), type :"string" });
        }
    });
    this.columnDefs.push({ headerName: '', type: 'button', awesome: 'fa-regular fa-trash-can', action: item => this.removeExecution(item)});
  }

  removeExecution(execution) {
    this.executions.splice(this.executions.indexOf(execution));
    this.table.refresh();
    this.areAllExecutionsValid();
  }

  // This method recalculates the number of executions that will be created
  recalculateExecutionNumber() {
    let nbDatasets = this.selectedDatasets.size;
    let nbDatasetsToSelectByExecution = 0;
    this.pipeline.parameters.forEach(parameter =>  {
        // Get parameter from executionForm
        if (parameter.type == ParameterType.File) {
            nbDatasetsToSelectByExecution +=1;
        }
    });
    this.nbExecutions = Math.floor(nbDatasets / nbDatasetsToSelectByExecution);
  }

  // Here we create a bunch of executions with default parameters
  onApplyParameters() {

    let availableDatasets: Dataset[] = Array.from(this.selectedDatasets);
    // By default, we order by alphabtical order
    // TODO: Propose another possible order (by ID?) 
    availableDatasets.sort((a: Dataset, b: Dataset) => {
      return a.name.localeCompare(b.name);
    })
    
    // Reset executions
    this.executions = [];

    for (let i = 0; i < this.nbExecutions; i++) {
    let execution: Execution = new Execution();
    
    execution.name = this.executionForm.get("execution_name").value;
    execution.pipelineIdentifier = this.pipeline.identifier;
    execution.timeout = 20;
    execution.inputValues = {};

    this.pipeline.parameters.forEach(
      parameter => {
        if (parameter.type == ParameterType.File) {
            // If we have a file, we try to set up the adapted dataset
          //let dataset = this.executionForm.get(parameter.name).value;
          // We try to find the first adapted dataset
          let datasetFilter: RegExp = new RegExp(this.executionForm.get(parameter.name).value);

          let index = availableDatasets.findIndex(ds => {
            return datasetFilter.test(ds.name);
          })
          let dataset;
          if (index != -1) {
              // Remove dataset and get it (should be of length one)
              dataset = availableDatasets.splice(index, 1)[0];              
          } else {
              // If regex is wrong / faulty
              dataset = availableDatasets.splice(0, 1)[0];
          }

          // TODO the format should be selected depending on the pipeline.
          // File ad md5 values should be selected automcatically depending on the pipeline.
          execution.inputValues[parameter.name] = this.getDatasetValue(dataset);
        } else if (parameter.type == ParameterType.Boolean) {
            execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value ? true : false;
        } else {
          execution.inputValues[parameter.name] = this.executionForm.get(parameter.name).value;
        }
      }
    )
    this.executions.push(execution);
    }
    this.parametersApplied = true;
    this.table.refresh();
    this.areAllExecutionsValid();
  }

  areAllExecutionsValid() {
    for (let param of this.pipeline.parameters) {
        if (param.isOptional || param.type == ParameterType.Boolean) {
            continue;
        }
        for (let exec of this.executions) {
            if(!exec.inputValues[param.name]) {
                this.allExecsValid = false;
                break;
            }
        }        
    }
    this.allExecsValid = true;
  }

  getDatasetValue(dataset) {
    // TODO the dataset extension format should be selected depending on the pipeline.
    let dataset_name = `id+${dataset.id}+${dataset.name.replace(/ /g,"_")}.nii.gz`
    return `shanoir:/${dataset_name}?format=nii&datasetId=${dataset.id}&token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
  }

  getPage() : Promise<Page<Execution>> {
        let page: Page<Execution> = new Page();
        page.content = this.executions;
        page.number = 1;
        page.size = this.executions.length;
        page.numberOfElements = this.executions.length;
        page.totalElements = this.executions.length;
        page.totalPages = Math.ceil(page.numberOfElements/page.size);
        return new Promise((resolve, reject) => {
            resolve(page);
        });
  }

  onSubmitExecutionForm() {
    /**
     * Init result location
     * The result directory should be dynamic
     */
    let promises: Promise<Execution>[] = [];
    let resultPath = this.generateResultPath();

    this.executions.forEach(exec => {
        exec.resultsLocation = `shanoir:/${resultPath}?token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
        promises.push(this.carminClientService.createExecution(exec));
    });

    Promise.all(promises).then(results => {
        let execution = results[0];
        this.msgService.log('info', 'the execution successfully started.')

        let carminDatasetProcessing: CarminDatasetProcessing = new CarminDatasetProcessing(execution.identifier, execution.name, execution.pipelineIdentifier, resultPath, execution.status, execution.timeout, execution.startDate, execution.endDate);
        
        carminDatasetProcessing.comment = execution.identifier;
        carminDatasetProcessing.studyId = [...this.selectedDatasets][0].study.id;  // TODO : this should be selected automatically if all datasets have the same study, if not show a select input to choose what context.
        carminDatasetProcessing.datasetProcessingType = DatasetProcessingType.SEGMENTATION; // TODO : this should be selected by the user.
        
        // HOTFIX for circular dataset object issue 
        this.selectedDatasets.forEach(dataset  => {
          dataset.study.subjectStudyList = [];
          dataset.study.studyCenterList = [];
          dataset.subject.subjectStudyList = [];
          this.selectedDatasets.add(dataset);
        })
    
        carminDatasetProcessing.inputDatasets = Array.from(this.selectedDatasets);
    
        this.carminDatasetProcessing.create(carminDatasetProcessing).then(
          (response: CarminDatasetProcessing) => {
            this.router.navigate([`/dataset-processing/details/${response.id}`]);
          },
          (error) => {
            this.msgService.log('error', 'Sorry, an error occurred while creating dataset processing.');
            console.error(error);
          }
        )
    })
  }

  getParameterType(parameterType: ParameterType): String {
    switch (parameterType) {
      case ParameterType.String:
      case ParameterType.Boolean: return 'text';
      case ParameterType.Int64:
      case ParameterType.Double: return 'number';
      case ParameterType.File: return 'file';
    }
  }

  isAFile(parameterType: ParameterType): boolean {
    if (parameterType == ParameterType.File) return true;
    return false;
  }

  private generateResultPath(){
    return this.keycloakService.getUserId() + "/" + Date.now();
  }

}
