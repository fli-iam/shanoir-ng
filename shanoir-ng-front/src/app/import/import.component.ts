import { Component, OnInit } from '@angular/core';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'imports',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css']
})
export class ImportComponent implements OnInit {

    private importMode: "DICOM" | "PACS";
    private title: string = '';

    constructor(private breadcrumbsService: BreadcrumbsService,
        private route: ActivatedRoute, private router: Router) {
            route.url.subscribe(() => {
                if (this.route.snapshot.firstChild && this.route.snapshot.firstChild.data['importMode']) {
                    this.title = 'from ' + this.route.snapshot.firstChild.data['importMode'];
                }
        })
    }
        
    ngOnInit() {
        this.breadcrumbsService.currentStep.importStart = true;  
    }
}