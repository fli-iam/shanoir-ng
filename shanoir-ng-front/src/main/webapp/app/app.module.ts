import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { routing } from './app.routing';

import { AppComponent }   from './app.component';
import { LoginComponent }   from './users/login/login.component';
import { HomeComponent }   from './home/home.component';
import { LoginService } from './shared/login/login.service';
import { AccountEventsService } from './users/account/account.events.service';

@NgModule({
    imports:      [ 
        BrowserModule,
        FormsModule,
        ReactiveFormsModule,
        HttpModule,
        routing
    ],
    declarations: [ 
        AppComponent,
        LoginComponent,
        HomeComponent
    ],
    providers: [
        LoginService,
        AccountEventsService
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }