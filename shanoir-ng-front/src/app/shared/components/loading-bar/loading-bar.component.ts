import { Component, Input } from '@angular/core';


@Component({
    selector: 'progress-bar',
    templateUrl: 'loading-bar.component.html',
    styleUrls: ['loading-bar.component.css']
})

export class LoadingBarComponent {

    @Input() progress: number = 0;

    getProgressText(): string {
        return Math.round(this.progress * 100) + "%";
    }

} 