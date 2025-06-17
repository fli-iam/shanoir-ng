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
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { Router } from '@angular/router';


@Component({
    selector: 'dua-assistant',
    templateUrl: 'dua-assistant.component.html',
    styleUrls: ['dua-assistant.component.css'],
    standalone: false
})

export class DUAAssistantComponent {


    public static openCreateDialog(studyId: number, confirmDialogService: ConfirmDialogService, router: Router) {
        confirmDialogService.confirm('Data User Agreement',
            'A Data User Agreement is strongly recommended for your study. '
            + 'Once set up it will be mandatory for any study member to agree it before accessing to the data. '
            + 'Do you want to start setting up one ?')
            .then(userChoice => {
                if (userChoice) {
                    router.navigate(['/dua/create/' + studyId]);
                }
            });
    }

}