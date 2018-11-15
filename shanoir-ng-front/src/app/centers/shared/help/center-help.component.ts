import { Component } from '@angular/core';

import { ImportDataService } from '../../../import/import.data-service';


@Component({
    selector: 'center-help',
    templateUrl: 'center-help.component.html',
    styleUrls: ['center-help.component.css']
})
export class CenterHelpComponent  {

    private institution: any;

    
    constructor(private importDataService: ImportDataService) {
        
        if (importDataService.patients && importDataService.patients[0]
                && importDataService.patients[0].studies && importDataService.patients[0].studies[0]
                && importDataService.patients[0].studies[0].series && importDataService.patients[0].studies[0].series[0]) {

            this.institution = importDataService.patients[0].studies[0].series[0].institution;
        }
    }
}