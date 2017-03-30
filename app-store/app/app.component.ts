import { Component } from '@angular/core';
import { AppStoreComponent } from './appStore/appStore.component'
import { SmallAppComponent } from './appStore/smallApp/smallApp.component'

@Component({
    selector: 'my-app',
    template: `
            <app-store></app-store>
    `,
    styleUrls: ['app/common.css'],
})
export class AppComponent { 
    
} 