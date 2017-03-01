import { Component } from '@angular/core';
import { Router } from '@angular/router';

import '../assets/css/common.css';

@Component({
    selector: 'shanoir-ng-app',
    templateUrl: 'app.component.html'
})

export class AppComponent {

    constructor(private router: Router) {
    }
    
}