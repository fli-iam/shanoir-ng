

// import 'buffer';

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { KeycloakService } from './app/shared/keycloak/keycloak.service';

if (environment.production) {
    enableProdMode();
}
if (window.location.href == window.origin + '/shanoir-ng/' 
        || window.location.href.endsWith('/welcome')
        || window.location.href.includes('/account-request')
        || window.location.href.endsWith('/extension-request')
        || window.location.href.endsWith('/challenge-request')) {
    // Public URL
    platformBrowserDynamic().bootstrapModule(AppModule);
} else if (window.location.href.includes('/dua/edit')
        || window.location.href.includes('/dua/view')) {
    // Can be either public or private
    KeycloakService.init(true)
    .then(() => {
        platformBrowserDynamic().bootstrapModule(AppModule);
    });
} else {
    // private 
    console.log(KeycloakService.auth)
    KeycloakService.init()
        .then(() => {
            platformBrowserDynamic().bootstrapModule(AppModule);
        });
  }