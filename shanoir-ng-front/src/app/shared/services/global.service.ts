import { Injectable, ElementRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';


@Injectable()
export class GlobalService {
    
    public onGlobalClick: Observable<Event>;
    
    constructor() { }

    registerGlobalClick(rootElement: ElementRef) {
        this.onGlobalClick = Observable.fromEvent(rootElement.nativeElement, 'click');
    }
    
}