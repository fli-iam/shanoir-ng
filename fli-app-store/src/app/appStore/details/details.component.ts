import { Component, ViewChild, ElementRef } from '@angular/core';

@Component({
    selector: 'app-details',
    templateUrl: 'app/appStore/details/details.component.html',
    styleUrls: ['app/appStore/details/details.component.css'],
})

export class DetailsComponent {

    @ViewChild('resume') resumeElt: ElementRef;
    @ViewChild('resumeWrap') resumeWrapElt: ElementRef;
    private displayElipsis: boolean = false;
    private resumeContentSave: string;
    private resumeOpened: boolean = false;

    constructor() {
    }

    ngAfterViewInit() {
         this.onResumeResize();
     }

    resumeOverflows(): boolean {
        return this.resumeElt.nativeElement.offsetHeight > 
            this.resumeWrapElt.nativeElement.offsetHeight;
    }

    onResumeResize() {
        if (!this.resumeOpened) {
            this.displayElipsis = false;
            if (this.resumeContentSave == undefined) this.resumeContentSave = this.resumeElt.nativeElement.innerText;
            this.restoreResume();
            let addElipsis = this.resumeOverflows();
            while (this.resumeOverflows()) {
                var lastIndex = this.resumeElt.nativeElement.innerText.lastIndexOf(" ");
                this.resumeElt.nativeElement.innerText 
                    = this.resumeElt.nativeElement.innerText.substring(0, lastIndex);
            }
            if (addElipsis) {
                var lastIndex = this.resumeElt.nativeElement.innerText.lastIndexOf(" ");
                this.resumeElt.nativeElement.innerText 
                    = this.resumeElt.nativeElement.innerText.substring(0, lastIndex);
                this.displayElipsis = true;
            }
        }
    }

    toggle(): void {
        if (this.resumeOpened) this.close();
        else this.open();
    }

    open(): void {
        this.restoreResume();
        this.resumeOpened = true;
    }

    close(): void {
        this.resumeOpened = false;
        this.onResumeResize();
    }

    restoreResume(): void {
        this.resumeElt.nativeElement.innerText = this.resumeContentSave;
    }
       
}