import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FooterState } from './footer-state.model';

@Component({
    selector: 'form-footer',
    templateUrl: 'form-footer.component.html'
})

export class FormFooterComponent {

    @Input() private state: FooterState;

    @Output() private save: EventEmitter<void> = new EventEmitter<void>();
    @Output() private create: EventEmitter<void> = new EventEmitter<void>();
    @Output() private update: EventEmitter<void> = new EventEmitter<void>();
    @Output() private edit: EventEmitter<void> = new EventEmitter<void>();
    @Output() private back: EventEmitter<void> = new EventEmitter<void>();

    
    private onCreate(): void {
        this.save.emit();
        this.create.emit();
    }

    private onUpdate(): void {
        this.save.emit();
        this.update.emit();
    }


    private onEdit(): void {
        this.edit.emit();
    }

    private onCancelEdit(): void {
        this.edit.emit();
    }

    private onBackToList(): void {
        this.back.emit();
    }

    private onBack(): void {
        this.back.emit();
    }

    
}