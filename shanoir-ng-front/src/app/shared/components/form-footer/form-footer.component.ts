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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FooterState } from './footer-state.model';

@Component({
    selector: 'form-footer',
    templateUrl: 'form-footer.component.html'
})

export class FormFooterComponent {

    @Input() state: FooterState;

    @Output() private save: EventEmitter<void> = new EventEmitter<void>();
    @Output() private create: EventEmitter<void> = new EventEmitter<void>();
    @Output() private update: EventEmitter<void> = new EventEmitter<void>();
    @Output() private edit: EventEmitter<void> = new EventEmitter<void>();
    @Output() private cancel: EventEmitter<void> = new EventEmitter<void>();
    @Output() private back: EventEmitter<void> = new EventEmitter<void>();
    @Output() private delete: EventEmitter<void> = new EventEmitter<void>();
    
    onCreate(): void {
        this.save.emit();
        this.create.emit();
    }

    onUpdate(): void {
        this.save.emit();
        this.update.emit();
    }

    onEdit(): void {
        this.edit.emit();
    }
   
     onDelete(): void {
        this.delete.emit();
    }

    onCancelEdit(): void {
        this.edit.emit();
    }

    onBackToList(): void {
        this.back.emit();
    }

    onCancel(): void {
        this.cancel.emit();
    }

    onBack(): void {
        this.back.emit();
    }
}