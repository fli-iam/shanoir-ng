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
import { Component } from '@angular/core';

import { preventInitialChildAnimations, slideDown } from '../../shared/animations/animations';
import { BasicClinicalContextComponent } from '../basic-clinical-context/basic-clinical-context.component';
import { ImportJob } from '../shared/dicom-data.model';
import { NgIf } from '@angular/common';
import { TooltipComponent } from '../../shared/components/tooltip/tooltip.component';
import { SelectBoxComponent } from '../../shared/select/select.component';
import { FormsModule } from '@angular/forms';


@Component({
    selector: 'pacs-clinical-context',
    templateUrl: '../clinical-context/clinical-context.component.html',
    styleUrls: ['../clinical-context/clinical-context.component.css', '../shared/import.step.css'],
    animations: [slideDown, preventInitialChildAnimations],
    imports: [NgIf, TooltipComponent, SelectBoxComponent, FormsModule]
})
export class PacsClinicalContextComponent extends BasicClinicalContextComponent {
    
    getNextUrl(): string {
        return '/imports/pacs';
    }

    protected buildImportJob(): ImportJob {
        const importJob: ImportJob = super.buildImportJob(Date.now());
        importJob.fromPacs = true;
        return importJob
    }
    
}
