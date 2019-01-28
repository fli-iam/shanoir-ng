import { Component, OnInit } from '@angular/core';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'imports',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css']
})
export class ImportComponent implements OnInit {

    public importMode: "dicom" | "pacs";

    constructor(private breadcrumbsService: BreadcrumbsService,
        private route: ActivatedRoute){
            route.url.subscribe(() => {this.importMode = this.route.snapshot.firstChild.data[0].importMode;})
        }
        
    ngOnInit() {
        this.breadcrumbsService.currentStep.importStart = true;  
    }
}