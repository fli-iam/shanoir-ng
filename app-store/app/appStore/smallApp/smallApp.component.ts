import { Component, Input, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';

@Component({
    selector: 'small-app',
    template: `
        <div class="img-container"><img src="{{img}}"
            [routerLink]="details" 
            [queryParams]=""
            routerLinkActive="active"
        /></div>
        <div #right class="right">
            <div #title class="title" [attr.title]="init && displayTitle() ? label: null"><span #labelContainer>{{label}}</span></div>
            <div class="author">{{author}}</div>
            <div class="note-container">
                <div class="note">&#9734;&#9734;&#9734;&#9734;&#9734;<div class="note-back" [style.width]="(rate*100)+'%'">&#9733;&#9733;&#9733;&#9733;&#9733;</div></div>
            </div>
            <div class="price">19.99 €<span class="price-compl"> / patient</span></div>
        </div>
    `,
    styles: [
        ':host() { max-width: 200px; display: block; float: left; margin: 15px; }',
        '.img-container { width: 60px; height: 60px; display: flex; margin-right: 15px; float: left; }',
        'img { max-width: 60px; max-height: 60px; width: auto; height: auto; display: block; vertical-align: middle; margin: auto; }',
        '.right { float: right; width: 125px; }',
        '.title { text-align: left; text-overflow: ellipsis; white-space: nowrap; overflow: hidden; font-weight: bold; }',
        '.author { color: #888; }',
        '.note-container { display: block; }',
        '.note { font-size: 16px; position: relative; display: inline-block; line-height: 15px;  }',
        '.note-back { font-size: 16px; position: absolute; top: 0; left: 0; overflow: hidden; width: 0%; z-index: -1; color: #888; }',
        '.price-compl { color: #888; font-size: 8px; } '
    ],
})

export class SmallAppComponent {

    @Input() rate: number;
    @Input() label: string;
    @Input() author: string;
    @Input() img: string;
    @ViewChild('right') rightContainer: ElementRef;
    @ViewChild('labelContainer') labelContainer: ElementRef;

    private init: boolean = false;

    constructor(private cdr: ChangeDetectorRef) {
    }

    displayTitle(): boolean {
        return this.labelContainer.nativeElement.offsetWidth > this.rightContainer.nativeElement.offsetWidth;
    }

     ngAfterViewInit() {
         this.init = true;
         this.cdr.detectChanges();
     }       
}