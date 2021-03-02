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
import { Injectable, ComponentFactoryResolver, ComponentRef } from '@angular/core';
import { ConfirmDialogComponent } from './confirm-dialog.component';
import { ServiceLocator } from '../../../utils/locator.service';


@Injectable()
export class ConfirmDialogService {
    
    constructor(private componentFactoryResolver: ComponentFactoryResolver) {
    }

    public confirm(title: string, message: string, buttons?: {ok: string, cancel: string}): Promise<boolean> {
        const componentFactory = this.componentFactoryResolver.resolveComponentFactory(ConfirmDialogComponent);
        const ref: ComponentRef<ConfirmDialogComponent> = ServiceLocator.rootViewContainerRef.createComponent(componentFactory);
        let dialog: ConfirmDialogComponent = ref.instance;
        return dialog.open(title, message, buttons).then(answer => {
            ref.destroy();
            return answer;
        });
    }

    
}