import { Component, Input, OnInit } from '@angular/core';
import { ImportDataService } from '../../import/import.data-service';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';

@Component({
    selector: 'help-message',
    templateUrl: 'help-message.component.html',
    styleUrls: ['help-message.component.css']
})
export class HelpMessageComponent implements OnInit {

    @Input() help: "institution" | "equipment";
    private message: any;
    private inImport: boolean;
    
    constructor(private importDataService: ImportDataService, private breadcrumbsService: BreadcrumbsService) {}

    ngOnInit() {
        if (this.importDataService.patients && this.importDataService.patients[0]
            && this.importDataService.patients[0].studies && this.importDataService.patients[0].studies[0]
            && this.importDataService.patients[0].studies[0].series && this.importDataService.patients[0].studies[0].series[0]) {
            
                if (this.help == 'institution' && this.importDataService.patients[0].studies[0].series[0].institution) {   
                this.message = this.importDataService.patients[0].studies[0].series[0].institution.institutionName + ", " +
                    this.importDataService.patients[0].studies[0].series[0].institution.institutionAddress;
            } else if (this.help == 'equipment' && this.importDataService.patients[0].studies[0].series[0].equipment) {
                this.message = this.importDataService.patients[0].studies[0].series[0].equipment.manufacturer + " - " +
                    this.importDataService.patients[0].studies[0].series[0].equipment.manufacturerModelName + " - " + 
                    this.importDataService.patients[0].studies[0].series[0].equipment.deviceSerialNumber;
            }
        }
        this.inImport = this.breadcrumbsService.isImporting();
    }
}