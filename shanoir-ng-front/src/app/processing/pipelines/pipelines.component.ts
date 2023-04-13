import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { Pipeline } from 'src/app/carmin/models/pipeline';
import { CarminClientService } from 'src/app/carmin/shared/carmin-client.service';
import { ProcessingService } from '../processing.service';

@Component({
  selector: 'app-pipelines',
  templateUrl: './pipelines.component.html',
  styleUrls: ['./pipelines.component.css']
})
export class PipelinesComponent implements OnInit {

  pipelines:Pipeline[];
  selectedPipeline:Pipeline;
  descriptionLoading:boolean;

  constructor(private breadcrumbsService: BreadcrumbsService,private carminClientService: CarminClientService, private router: Router, private processingService:ProcessingService) { 
    this.pipelines = [];
    this.descriptionLoading = false;
    
    this.breadcrumbsService.currentStepAsMilestone();
    this.breadcrumbsService.nameStep('1. Processing');
  }

  ngOnInit(): void {
    this.carminClientService.listPipelines().subscribe(
      (pipelines :Pipeline[])=>{
        this.pipelines = pipelines;
      }
    )
  }

  selectPipeline(pipeline:Pipeline) {
    this.descriptionLoading = true;
    this.carminClientService.getPipeline(pipeline.identifier).subscribe(
      (pipeline:Pipeline)=>{
        this.descriptionLoading = false;
        this.selectedPipeline = pipeline;
      },
      (error)=>{
        console.error(error);
      }
    )
  }

  choosePipeLine(){
    this.processingService.setPipeline(this.selectedPipeline);
    let filesParam = 0;
    let datsetNumbers = this.processingService.selectedDatasetsValue?.size;
    // Here we are going to calculate the number of possible executions in parralel
    this.selectedPipeline.parameters?.forEach(parameter => {
        if (parameter.type == 'File') {
            filesParam += 1;
        }
    })
    this.router.navigate(['processing/execution']);
  }

}
