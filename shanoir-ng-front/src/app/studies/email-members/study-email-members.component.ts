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

import { Component, Input } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { TooltipComponent } from '../../shared/components/tooltip/tooltip.component';
import { MassEmailRequest } from '../../users/mass-email/mass-email.model';
import { MassEmailService } from '../../users/mass-email/mass-email.service';
import { Study } from '../shared/study.model';

@Component({
    selector: 'study-email-members',
    templateUrl: 'study-email-members.component.html',
    imports: [FormsModule, ReactiveFormsModule, TooltipComponent]
})

export class StudyEmailMembersComponent {

    @Input() study: Study;

    form: UntypedFormGroup;
    sending: boolean = false;

    constructor(private massEmailService: MassEmailService,
            private confirmDialogService: ConfirmDialogService,
            private msgBoxService: MsgBoxService,
            private formBuilder: UntypedFormBuilder) {

        this.buildForm();
    }

    buildForm(): void {
        this.form = this.formBuilder.group({
            'subject': ['', [Validators.required, Validators.maxLength(255)]],
            'content': ['', [Validators.required]],
        });
    }

    get memberCount(): number {
        return this.study?.studyUserList?.length ?? 0;
    }

    send(): void {
        this.confirmDialogService.confirm('Send mass email',
                'This email will be sent to the ' + this.memberCount + ' members of ' + this.study.name + '. Proceed?')
            .then(confirmed => {
                if (!confirmed) return;
                this.sending = true;
                const request = new MassEmailRequest();
                request.recipientGroup = 'STUDY';
                request.studyId = this.study.id;
                request.subject = this.form.get('subject').value;
                request.content = this.form.get('content').value;
                this.massEmailService.sendMassEmail(request)
                    .then(count => {
                        this.msgBoxService.log('info', 'Email queued for ' + count + ' users');
                        this.form.reset({ subject: '', content: '' });
                    })
                    .catch(() => this.msgBoxService.log('error', 'The email could not be sent'))
                    .finally(() => this.sending = false);
            });
    }

    formErrors(field: string): any {
        if (!this.form) return;
        const control = this.form.get(field);
        if (control && control.touched && !control.valid) {
            return control.errors;
        }
    }

    hasError(fieldName: string, errors: string[]) {
        const formError = this.formErrors(fieldName);
        if (formError) {
            for (const errorName of errors) {
                if (formError[errorName]) return true;
            }
        }
        return false;
    }
}
