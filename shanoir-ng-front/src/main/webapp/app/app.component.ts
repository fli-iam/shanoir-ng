import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'shanoir-ng-app',
    moduleId: module.id,
    templateUrl: 'app.component.html'
})

export class AppComponent {

    constructor(private router: Router) {
    }
    
}