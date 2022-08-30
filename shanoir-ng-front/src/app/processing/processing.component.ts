import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { ProcessingService } from './processing.service';

@Component({
  selector: 'app-processing',
  templateUrl: './processing.component.html',
  styleUrls: ['./processing.component.css']
})
export class ProcessingComponent implements OnInit {

  error:boolean;

  constructor(private router: Router, private activatedRoute: ActivatedRoute, private processingService: ProcessingService) {
    this.error = false; 
   }

  ngOnInit(): void {
    if(this.processingService.isDatasetsSubjectValid()){
      this.error = false;
      this.router.navigate(['pipelines'], {relativeTo: this.activatedRoute});
    }else{
      this.error = true;
    }
  }

  navigateToSolr(): void{
    this.router.navigate(['/solr-search']);
  }

}
