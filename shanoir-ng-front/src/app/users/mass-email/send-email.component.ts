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
import { UntypedFormBuilder, UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { TooltipComponent } from '../../shared/components/tooltip/tooltip.component';

import { MassEmailRequest, RecipientGroup } from './mass-email.model';
import { MassEmailService } from './mass-email.service';

@Component({
    selector: 'send-email',
    templateUrl: 'send-email.component.html',
    imports: [FormsModule, ReactiveFormsModule, TooltipComponent]
})

export class SendEmailComponent {

    form: UntypedFormGroup;
    counts: Partial<Record<RecipientGroup, number>> = {};
    sending: boolean = false;
    readonly groups: { value: RecipientGroup, label: string }[] = [
        { value: 'ALL', label: 'All users' },
        { value: 'ACTIVE', label: 'Active users' },
        { value: 'INACTIVE', label: 'Inactive users' },
    ];

    constructor(private massEmailService: MassEmailService,
            private confirmDialogService: ConfirmDialogService,
            private msgBoxService: MsgBoxService,
            private breadcrumbsService: BreadcrumbsService,
            private formBuilder: UntypedFormBuilder) {

        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = 'Send email';
        });
        this.buildForm();
        this.loadCounts();
    }

    buildForm(): void {
        this.form = this.formBuilder.group({
            'recipientGroup': ['ALL', [Validators.required]],
            'subject': ['', [Validators.required, Validators.maxLength(255)]],
            'content': ['', [Validators.required]],
        });
    }

    private loadCounts(): void {
        for (const group of this.groups) {
            this.massEmailService.countRecipients(group.value)
                .then(count => this.counts[group.value] = count)
                .catch(() => { /* count stays unknown, displayed as (?) */ });
        }
    }

    countLabel(group: RecipientGroup): string {
        return '(' + (this.counts[group] !== undefined ? this.counts[group] : '?') + ')';
    }

    send(): void {
        const group: RecipientGroup = this.form.get('recipientGroup').value;
        const groupLabel: string = this.groups.find(g => g.value == group).label.toLowerCase();
        const countTxt: string = this.counts[group] !== undefined ? this.counts[group] + ' users' : 'the users';
        this.confirmDialogService.confirm('Send mass email',
                'This email will be sent to ' + countTxt + ' (' + groupLabel + '). Proceed?')
            .then(confirmed => {
                if (!confirmed) return;
                this.sending = true;
                const request = new MassEmailRequest();
                request.recipientGroup = group;
                request.subject = this.form.get('subject').value;
                request.content = this.form.get('content').value;
                this.massEmailService.sendMassEmail(request)
                    .then(count => {
                        this.msgBoxService.log('info', 'Email queued for ' + count + ' users');
                        this.form.reset({ recipientGroup: 'ALL', subject: '', content: '' });
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