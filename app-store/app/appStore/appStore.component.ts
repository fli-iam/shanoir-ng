import { Component } from '@angular/core';
import { SmallAppComponent } from './smallApp/smallApp.component'

@Component({
    selector: 'app-store',
    templateUrl: 'app/appStore/appStore.component.html',
    styleUrls: ['app/appStore/appStore.component.css'],
})

export class AppStoreComponent {

    testArr = [1, 2, 3, 4, 5, 6];

    constructor() {
    }
       
}