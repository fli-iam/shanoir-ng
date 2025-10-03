/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import {Component, OnInit} from '@angular/core';
import {Router} from '@angular/router';
import {BreadcrumbsService} from 'src/app/breadcrumbs/breadcrumbs.service';
import {Pipeline} from 'src/app/vip/models/pipeline';
import {ExecutionDataService} from '../execution.data-service';
import {PipelineService} from "./pipeline/pipeline.service";

@Component({
    selector: 'app-pipelines',
    templateUrl: './pipelines.component.html',
    styleUrls: ['./pipelines.component.css'],
    standalone: false
})
export class PipelinesComponent implements OnInit {

  pipelines:Pipeline[];
  selectedPipeline:Pipeline;
  descriptionLoading:boolean;

  constructor(private breadcrumbsService: BreadcrumbsService, private pipelineService: PipelineService, private router: Router, private processingService:ExecutionDataService) {
    this.pipelines = [];
    this.descriptionLoading = false;

    this.breadcrumbsService.currentStepAsMilestone();
    this.breadcrumbsService.nameStep('1. Processing');
  }

  ngOnInit(): void {
    this.pipelineService.listPipelines().subscribe(
      (pipelines :Pipeline[])=>{
        this.pipelines = pipelines;
      }
    )
  }

  selectPipeline(pipeline:Pipeline) {
    this.descriptionLoading = true;
    this.pipelineService.getPipeline(pipeline.identifier).subscribe(
      (pipeline:Pipeline)=>{
        this.descriptionLoading = false;
        this.selectedPipeline = pipeline;
      },
      (error)=>{
        console.error(error);
      }
    )
  }

  isSelectedDatasets(): boolean {
      return this.processingService.selectedDatasets && this.processingService.selectedDatasets.size > 0;
  }

  choosePipeLine(){
    this.processingService.setPipeline(this.selectedPipeline);
    let filesParam = 0;
    // Here we are going to calculate the number of possible executions in parallel
    this.selectedPipeline.parameters?.forEach(parameter => {
        if (parameter.type == 'File') {
            filesParam += 1;
        }
    })
    this.router.navigate(['execution']);
  }

  navigateToSolr(): void{
        this.router.navigate(['/solr-search']);
    }
}
