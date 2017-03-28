import { Component, Input } from '@angular/core';


@Component({
    selector: 'progress-bar',
    template: `
        <span class="inner" [style.width]="progress*200">&nbsp;</span><span class="text">{{getProgressText()}}</span>
    `,
    styles: [
        ':host() { position: relative; background-color: var(--light-grey); width: 200px; display: inline-block; height: 18px; border: 1px solid var(--grey); box-shadow: 0 0 5px 0 var(--shadow-color) inset; border-radius: 2px; }',
        '.inner { background-color: olivedrab; display: inline-block; height: 100%; /*box-shadow: 0 0 5px 0 var(--shadow-color);*/ }',
        '.text { color: var(--color-a); position: absolute; left: 0; right: 0; top: 0; bottom: 0; text-align: center; vertical-align: middle; line-height: -moz-block-height; line-height: 18px; }'
    ]
})

export class LoadingBarComponent {

    @Input() progress: number = 0;

    getProgressText(): string {
        return Math.round(this.progress*100)+"%";
    }

} 