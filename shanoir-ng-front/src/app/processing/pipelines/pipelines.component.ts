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

  selectPipeline(pipeline:Pipeline){
    this.descriptionLoading = true;
    this.carminClientService.getPipeline(pipeline.identifier).subscribe(
      (pipeline:Pipeline)=>{
        this.descriptionLoading = false;
        this.selectedPipeline = pipeline;
        console.log(pipeline);
      },
      (error)=>{
        console.error(error);
      }
    )
  }

  choosePipeLine(){
    this.processingService.setPipeline(this.selectedPipeline);
    this.router.navigate(['processing/execution']);
  }

}
