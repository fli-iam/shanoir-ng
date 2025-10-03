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
import { Injectable, ComponentRef } from '@angular/core';

import { ServiceLocator } from '../../../utils/locator.service';

import { ConfirmDialogComponent } from './confirm-dialog.component';


@Injectable()
export class ConfirmDialogService {

    public confirm(title: string, message?: string, buttons?: {yes: string, cancel: string}): Promise<boolean> {
        const ref: ComponentRef<ConfirmDialogComponent> = ServiceLocator.rootViewContainerRef.createComponent(ConfirmDialogComponent);
        let dialog: ConfirmDialogComponent = ref.instance;
        return dialog.openConfirm(title, message, buttons).then(answer => {
            ref.destroy();
            return answer;
        });
    }

    public choose(title: string, message?: string, buttons?: {yes: string, no: string, cancel?: string}): Promise<'yes' | 'no' | false> {
        const ref: ComponentRef<ConfirmDialogComponent> = ServiceLocator.rootViewContainerRef.createComponent(ConfirmDialogComponent);
        let dialog: ConfirmDialogComponent = ref.instance;
        return dialog.openChoose(title, message, buttons).then(answer => {
            ref.destroy();
            return answer;
        });
    }

    public inform(title: string, message?: string, button?: string): Promise<boolean> {
        const ref: ComponentRef<ConfirmDialogComponent> = ServiceLocator.rootViewContainerRef.createComponent(ConfirmDialogComponent);
        let dialog: ConfirmDialogComponent = ref.instance;
        return dialog.openInfo(title, message, button).then(answer => {
            ref.destroy();
            return answer;
        });
    }

    public error(title: string, message?: string, link?: string): Promise<boolean> {
        const ref: ComponentRef<ConfirmDialogComponent> = ServiceLocator.rootViewContainerRef.createComponent(ConfirmDialogComponent);
        let dialog: ConfirmDialogComponent = ref.instance;
        return dialog.openError(title, message, link).then(answer => {
            ref.destroy();
            return answer;
        });
    }
    
}