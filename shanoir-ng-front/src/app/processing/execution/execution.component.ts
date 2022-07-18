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
import { KeycloakService } from 'src/app/shared/keycloak/keycloak.service';
import { MsgBoxService } from 'src/app/shared/msg-box/msg-box.service';
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

  constructor(private breadcrumbsService: BreadcrumbsService, private processingService: ProcessingService, private carminClientService: CarminClientService, private router: Router, private msgService: MsgBoxService, private keycloakService: KeycloakService, private datasetService: DatasetService, private carminDatasetProcessing: CarminDatasetProcessingService) {
    this.breadcrumbsService.nameStep('2. Execution');
    this.selectedDatasets = new Set<Dataset>();
  }

  ngOnInit(): void {
    if (!this.processingService.isAnyPipelineSelected()) {
      this.router.navigate(["/processing/pipelines"])
    }

    this.processingService.selectedPipeline.subscribe(
      (pipeline: Pipeline) => {
        this.pipeline = pipeline;
        this.initExecutionForm()
      }
    )
    this.processingService.selectedDatasets.subscribe(
      (datasets: Set<number>) => {
        let selectedDatasets = new Set<Dataset>();
        datasets.forEach(
          id => {
            this.datasetService.get(id).then((dataset: Dataset) => {
              selectedDatasets.add(dataset);
            })
          }
        )
        this.selectedDatasets = selectedDatasets;
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
        if (!parameter.isOptional) validators.push(Validators.required);
        let control = new FormControl(parameter.defaultValue, validators);
        if (parameter.name != "executable") this.executionForm.addControl(parameter.name, control);
      }
    )
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
          if (parameter.name == "executable") {
            // change the execution if you want to run another pipeline
            execution.inputValues[parameter.name] = "file:/var/www/html/workflows/SharedData/groups/Support/Applications/testGME2inputFilesUpdated/1.0/bin/testGME2inputFilesUpdated.sh.tar.gz";

          } else {
            let dataset = this.executionForm.get(parameter.name).value;
            let dataset_name = `id+${dataset.id}+${dataset.name.replace(/ /g,"_")}.nii.zip`
            execution.inputValues[parameter.name] = `shanoir:/${dataset_name}?format=nii&datasetId=${dataset.id}&token=${this.token}&refreshToken=${this.refreshToken}&md5=none&type=File`;
          }
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
    this.carminClientService.createExecution(execution).subscribe(
      (execution: Execution) => {
        this.msgService.log('info', 'the execution successfully started.')

        let carminDatasetProcessing: CarminDatasetProcessing = new CarminDatasetProcessing(execution.identifier, execution.name, execution.pipelineIdentifier, resultPath, execution.status, execution.timeout, execution.startDate, execution.endDate);
        
        carminDatasetProcessing.comment = execution.identifier;
        carminDatasetProcessing.studyId = [...this.selectedDatasets][0].study.id;
        carminDatasetProcessing.datasetProcessingType = DatasetProcessingType.SEGMENTATION;
        carminDatasetProcessing.inputDatasets = Array.from(this.selectedDatasets);

        this.carminDatasetProcessing.saveNewCarminDatasetProcessing(carminDatasetProcessing).subscribe(
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
