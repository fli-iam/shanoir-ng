import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { routing } from './app.routing';

import { AppComponent }   from './app.component';
import { HeaderComponent }   from './shared/header/header.component';
import { HomeComponent }   from './home/home.component';
import { LoginComponent }   from './users/login/login.component';
import { NavbarComponent }   from './shared/navbar/navbar.component';
import { AccountEventsService } from './users/account/account.events.service';
import { LoginService } from './shared/login/login.service';

@NgModule({
    imports:      [ 
        BrowserModule,
        FormsModule,
        HttpModule,
        ReactiveFormsModule,
        routing
    ],
    declarations: [ 
        AppComponent,
        HeaderComponent,
        HomeComponent,
        LoginComponent,
        NavbarComponent
    ],
    providers: [
        AccountEventsService,
        LoginService
    ],
    bootstrap:    [ AppComponent ]
})
export class AppModule { }