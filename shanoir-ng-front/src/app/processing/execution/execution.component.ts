import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { Execution } from 'src/app/carmin/models/execution';
import { ParameterType } from 'src/app/carmin/models/parameterType';
import { Pipeline } from 'src/app/carmin/models/pipeline';
import { CarminClientService } from 'src/app/carmin/shared/carmin-client.service';
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

  constructor(private breadcrumbsService: BreadcrumbsService, private processingService: ProcessingService, private carminClientService:CarminClientService, private router:Router,private msgService: MsgBoxService) { 
    this.breadcrumbsService.nameStep('2. Execution');
  }

  ngOnInit(): void {
    if(!this.processingService.isAnyPipelineSelected()){
      this.router.navigate(["/processing/pipelines"])
    }

    this.processingService.selectedPipeline.subscribe(
      (pipeline: Pipeline)=>{
        this.pipeline = pipeline;
        this.initExecutionForm()
      }
    )
  }

  initExecutionForm(){
    this.executionForm = new FormGroup({
      "execution_name": new FormControl('', Validators.required)
    });

    this.pipeline.parameters.forEach(
      parameter=>{
        let control = new FormControl(parameter.defaultValue);
        let validators:Validators[] = [];
        if(!parameter.isOptional) validators.push(Validators.required);
        this.executionForm.addControl(parameter.name, control);
      }
    )
  }

  onSubmitExecutionForm(){
    let execution:Execution = new Execution();
    execution.name = this.executionForm.get("execution_name").value;
    execution.pipelineIdentifier = this.pipeline.identifier;
    execution.timeout = 20;
    execution.inputValues = {};
    this.pipeline.parameters.forEach(
      parameter=>{
        if(parameter.name == "file"){
          execution.inputValues[parameter.name]= "/vip/Home/lorem_ipsum.txt";
        }else{
          execution.inputValues[parameter.name]=this.executionForm.get(parameter.name).value;
        }
      }
    )
    this.carminClientService.createExecution(execution).subscribe(
      (_)=>{
          console.log("executed !");
          this.msgService.log('info', 'the execution successfully started.')
      },
      (error)=>{
          this.msgService.log('error', 'Sorry, an error occurred while starting the execution.');
      }
  )
  }

  getParameterType(parameterType: ParameterType){
    switch(parameterType){
      case ParameterType.String:
      case ParameterType.Boolean: return 'text';
      case ParameterType.Int64:
      case ParameterType.Double: return 'number';
      case ParameterType.File: return 'file';
    }
  }

}
