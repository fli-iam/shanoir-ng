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
import { Directive, Input, TemplateRef, ViewContainerRef } from '@angular/core';

@Directive ({
    selector: '[ngVar]',
})
export class VarDirective {
    @Input() set ngVar(context: unknown) {
        this.context.$implicit = this.context.ngVar = context;

        if (!this.hasView) {
            this.vcRef.createEmbeddedView(this.templateRef, this.context);
            this.hasView = true;
        }
    }

    private context: {
        $implicit: unknown;
        ngVar: unknown;
    } = {
        $implicit: null,
        ngVar: null,
    };

    private hasView: boolean = false;

    constructor(
        private templateRef: TemplateRef<any>,
        private vcRef: ViewContainerRef
    ) {}
}