import { Component } from '@angular/core';
import { slideDown } from '../animations/animations';
import { MsgBoxService } from './msg-box.service';

@Component({
    selector: 'msg-box',
    templateUrl: './msg-box.component.html',
    styleUrls: ['./msg-box.component.css'],
    animations: [ slideDown ]
})
export class MsgBoxComponent {


    constructor(private msgboxService: MsgBoxService) {
        
    }    
}
