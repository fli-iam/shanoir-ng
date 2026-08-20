/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';

import { LocalDateFormatPipe } from '../../localLanguage/localDateFormat.pipe';

export interface DateRange {
    start: Date;
    end: Date;
}

type Handle = 'start' | 'end';

const DAY_MS = 24 * 60 * 60 * 1000;

@Component({
    selector: 'date-range-slider',
    templateUrl: 'date-range-slider.component.html',
    styleUrls: ['date-range-slider.component.css'],
    imports: [LocalDateFormatPipe]
})
export class DateRangeSliderComponent implements OnChanges {

    @Input() minDate: Date;
    @Input() maxDate: Date;
    @Input() startDate: Date;
    @Input() endDate: Date;
    @Output() rangeChange: EventEmitter<DateRange> = new EventEmitter();

    @ViewChild('track') trackRef: ElementRef<HTMLDivElement>;

    draftStart: Date;
    draftEnd: Date;
    private dragging: Handle | null = null;
    private boundOnPointerMove = (event: PointerEvent) => this.onPointerMove(event);
    private boundOnPointerUp = () => this.onPointerUp();

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['startDate'] || changes['endDate']) {
            this.draftStart = this.startDate;
            this.draftEnd = this.endDate;
        }
    }

    startPercent(): number {
        return this.toPercent(this.draftStart);
    }

    endPercent(): number {
        return this.toPercent(this.draftEnd);
    }

    private toPercent(date: Date): number {
        if (!this.minDate || !this.maxDate || !date) return 0;
        const span = this.maxDate.getTime() - this.minDate.getTime();
        if (span <= 0) return 0;
        return Math.min(100, Math.max(0, ((date.getTime() - this.minDate.getTime()) / span) * 100));
    }

    private toDate(percent: number): Date {
        const span = this.maxDate.getTime() - this.minDate.getTime();
        const time = this.minDate.getTime() + (span * percent) / 100;
        return this.startOfDay(new Date(time));
    }

    private startOfDay(date: Date): Date {
        return new Date(date.getFullYear(), date.getMonth(), date.getDate());
    }

    onHandlePointerDown(handle: Handle, event: PointerEvent): void {
        event.preventDefault();
        (event.currentTarget as HTMLElement).focus();
        this.dragging = handle;
        document.addEventListener('pointermove', this.boundOnPointerMove);
        document.addEventListener('pointerup', this.boundOnPointerUp);
    }

    private onPointerMove(event: PointerEvent): void {
        if (!this.dragging || !this.trackRef) return;
        const rect = this.trackRef.nativeElement.getBoundingClientRect();
        const percent = ((event.clientX - rect.left) / rect.width) * 100;
        const date = this.toDate(Math.min(100, Math.max(0, percent)));
        this.applyDrag(this.dragging, date);
    }

    private applyDrag(handle: Handle, date: Date): void {
        if (handle === 'start') {
            this.draftStart = date > this.draftEnd ? this.draftEnd : date;
        } else {
            this.draftEnd = date < this.draftStart ? this.draftStart : date;
        }
    }

    private onPointerUp(): void {
        document.removeEventListener('pointermove', this.boundOnPointerMove);
        document.removeEventListener('pointerup', this.boundOnPointerUp);
        if (this.dragging) {
            this.dragging = null;
            this.emitIfChanged();
        }
    }

    onHandleKeyDown(handle: Handle, event: KeyboardEvent): void {
        const current = handle === 'start' ? this.draftStart : this.draftEnd;
        let next: Date | null = null;
        switch (event.key) {
            case 'ArrowLeft':
            case 'ArrowDown':
                next = new Date(current.getTime() - DAY_MS);
                break;
            case 'ArrowRight':
            case 'ArrowUp':
                next = new Date(current.getTime() + DAY_MS);
                break;
            case 'Home':
                next = this.minDate;
                break;
            case 'End':
                next = this.maxDate;
                break;
            default:
                return;
        }
        event.preventDefault();
        next = new Date(Math.min(Math.max(next.getTime(), this.minDate.getTime()), this.maxDate.getTime()));
        this.applyDrag(handle, next);
        this.emitIfChanged();
    }

    reset(): void {
        this.draftStart = this.minDate;
        this.draftEnd = this.maxDate;
        this.emitIfChanged();
    }

    private emitIfChanged(): void {
        if (this.draftStart.getTime() !== this.startDate?.getTime() || this.draftEnd.getTime() !== this.endDate?.getTime()) {
            this.rangeChange.emit({ start: this.draftStart, end: this.draftEnd });
        }
    }
}
