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
import { Component, DestroyRef, ElementRef, EventEmitter, HostListener, Input, OnInit, Output, ViewChild } from '@angular/core';
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { Interval } from '../shared/quality-card.service';
import { GlobalService } from '../../shared/services/global.service';



@Component({
    selector: 'test-quality-card-options',
    templateUrl: 'test-quality-card-options.component.html',
    styleUrls: ['test-quality-card-options.component.css'],
    standalone: false
})
export class TestQualityCardOptionsComponent implements OnInit {

    @Input() nbExaminations: number;
    @Output() test: EventEmitter<Interval> = new EventEmitter();
    @Output() closeModal: EventEmitter<void> = new EventEmitter();
    form: UntypedFormGroup
    @ViewChild('window') window: ElementRef;

    constructor(private formBuilder: UntypedFormBuilder, globalService: GlobalService, private destroyRef: DestroyRef) {
        globalService.onNavigate
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
            this.cancel();
        });
    }

    ngOnInit(): void {
        this.form = this.buildForm();
    }
    
    private buildForm(): UntypedFormGroup {
        const formGroup = this.formBuilder.group({
            'from': [1],
            'to': [this.nbExaminations > 20 ? 20 : this.nbExaminations],
        });
        formGroup.controls.from.setValidators([
            Validators.required, 
            Validators.min(1), 
            (control: AbstractControl) => Validators.max(formGroup.get('to').value)(control)
        ]);
        formGroup.controls.to.setValidators([
            Validators.required,
            (control: AbstractControl) => Validators.min(formGroup.get('from').value)(control)
        ]);
        if (this.nbExaminations) {
            formGroup.controls.to.addValidators([Validators.max(this.nbExaminations)]);
        }
        return formGroup;
    };

    testOnAll() {
        this.test.emit();
    }

    testOnInterval() {
        this.test.emit(new Interval(this.form.get('from').value, this.form.get('to').value));
    }

    cancel() {
        this.closeModal.emit();
    }

    @HostListener('click', ['$event'])
    onClick(clickEvent) {
        if (!this.window.nativeElement.contains(clickEvent.target)) {
            this.cancel();
        }
    }   
    
    updateValidity() {
        this.form.controls.from.updateValueAndValidity();
        this.form.controls.to.updateValueAndValidity();
    }
}