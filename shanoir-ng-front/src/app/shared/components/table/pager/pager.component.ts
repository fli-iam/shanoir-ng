import { Component, EventEmitter, Input, Output, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'shanoir-pager',
    templateUrl: 'pager.component.html',
    styleUrls: ['pager.component.css'],
})

export class PagerComponent implements OnChanges {
    
    @Input() currentPage: number;
    @Input() nbPages: number;
    @Output() pageChange: EventEmitter<number> = new EventEmitter<number>();

    private pagerList: number [] = [];


    constructor() {
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['currentPage'] || changes['nbPages'] && this.currentPage && this.nbPages) {
            this.refreshPagerList();
        }
    }


    private refreshPagerList() {
        let nbLinks = 7; // Must be odd
        let half = (Math.floor(nbLinks / 2));
        let list: number[] = [];
        if (this.currentPage < half + 2) {
            if (this.nbPages < nbLinks) {
                for (let i = 1; i < nbLinks && i <= this.nbPages; i++) {
                    list.push(i);
                }
            } else {
                for (let i = 1; i < nbLinks - 1; i++) {
                    list.push(i);
                }
                list.push(null);
                list.push(this.nbPages);
            }
        } else {
            list.push(1);
            list.push(null);
            if (this.nbPages <= this.currentPage + half) {
                for (let i = this.nbPages - nbLinks + 3; i <= this.nbPages; i++) {
                    list.push(i);
                }
            } else {
                for (let i = this.currentPage - half + 2; i < this.currentPage + half - 1; i++) {
                    list.push(i);
                }
                list.push(null);
                list.push(this.nbPages);
            }
        }
        this.pagerList = list;
    }


    private goToPage(page: number) {
        this.pageChange.emit(page);
    }

}