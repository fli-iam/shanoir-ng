import { Component, Input, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';

@Component({
    selector: 'small-app',
    template: `
        <div class="img-container" 
            routerLink="details/{{app.id}}" 
            [queryParams]=""
            routerLinkActive="active">
                <img src="{{app.logo}}"/>
        </div>
        <div #right class="right">
            <div #title class="title" [attr.title]="init && displayTitle() ? app.label: null"><span #labelContainer>{{app.label}}</span></div>
            <div class="author">{{app.author}}</div>
            <div class="note-container">
                <div #note class="note" (click)="doRate($event)">&#9734;&#9734;&#9734;&#9734;&#9734;<div class="note-back" 
                [class.my-rate]="myRate != undefined" [style.width]="(myRate?myRate:app.review.rate)*100+'%'">&#9733;&#9733;&#9733;&#9733;&#9733;</div></div>
                <span class="reviewers" *ngIf="app.review.reviewers">({{app.review.reviewers}})</span>
            </div>
            <div class="price">19.99 €<span class="price-compl"> / patient</span></div>
        </div>
    `,
    styles: [
        ':host() { max-width: 200px; display: block; float: left; margin: 15px; }',
        '.img-container { width: 60px; height: 60px; display: flex; margin-right: 15px; float: left; }',
        'img { max-width: 60px; max-height: 60px; width: auto; height: auto; display: block; vertical-align: middle; margin: auto; cursor: pointer; }',
        '.right { float: right; width: 125px; }',
        '.title { text-align: left; text-overflow: ellipsis; white-space: nowrap; overflow: hidden; font-weight: bold; }',
        '.author { color: #888; }',
        '.note-container { display: block; }',
        '.note { font-size: 16px; position: relative; display: inline-block; line-height: 15px; cursor: pointer; vertical-align: middle; }',
        '.note-back { font-size: 16px; position: absolute; top: 0; left: 0; overflow: hidden; width: 0%; z-index: -1; color: #888; }',
        '.note-back.my-rate { color: goldenrod; }',
        '.reviewers { color: #888; font-size: 8px; vertical-align: text-bottom; }',
        '.price-compl { color: #888; font-size: 8px; }'
    ],
})

export class SmallAppComponent {

    @Input() app: Object = {id: 0};
    @ViewChild('right') rightContainer: ElementRef;
    @ViewChild('labelContainer') labelContainer: ElementRef;
    @ViewChild('note') noteElt: ElementRef;

    private init: boolean = false;
    public myRate: number;

    constructor(private cdr: ChangeDetectorRef) {
    }

    displayTitle(): boolean {
        return this.labelContainer.nativeElement.offsetWidth > this.rightContainer.nativeElement.offsetWidth;
    }

     ngAfterViewInit() {
         this.init = true;
         this.cdr.detectChanges();
     }

     doRate(event: MouseEvent) {
         this.myRate = event.offsetX / this.noteElt.nativeElement.offsetWidth;
     }
}