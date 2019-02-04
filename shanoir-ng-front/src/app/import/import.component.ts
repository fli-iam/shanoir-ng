import { Component, OnInit } from '@angular/core';
import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
    selector: 'imports',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css']
})
export class ImportComponent implements OnInit {

    public importMode: "DICOM" | "PACS";

    constructor(private breadcrumbsService: BreadcrumbsService,
        private route: ActivatedRoute, private router: Router) {
            route.url.subscribe(() => {
                if (this.route.snapshot.firstChild) {
                    this.importMode = this.route.snapshot.firstChild.data['importMode'];
                } else {
                    this.router.navigate(['home'], {replaceUrl: true});
                    return;
                }
        })
    }
        
    ngOnInit() {
        this.breadcrumbsService.currentStep.importStart = true;  
    }
}