import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FooterState } from './footer-state.model';

@Component({
    selector: 'form-footer',
    templateUrl: 'form-footer.component.html'
})

export class FormFooterComponent {

    @Input() private state: FooterState;
    @Output() private create: EventEmitter<void> = new EventEmitter<void>();
    @Output() private update: EventEmitter<void> = new EventEmitter<void>();
    @Output() private edit: EventEmitter<void> = new EventEmitter<void>();
    @Output() private back: EventEmitter<void> = new EventEmitter<void>();

    
}