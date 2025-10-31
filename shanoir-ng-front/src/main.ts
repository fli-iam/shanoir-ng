import { Buffer } from 'buffer';
(window as any).Buffer = Buffer;

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import localeDe from '@angular/common/locales/de';
import localeEs from '@angular/common/locales/es';

import { KeycloakService } from './app/shared/keycloak/keycloak.service';
import { environment } from './environments/environment';
import { AppModule } from './app/app.module';

registerLocaleData(localeFr);
registerLocaleData(localeDe);
registerLocaleData(localeEs);

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
    KeycloakService.init()
        .then(() => {
            platformBrowserDynamic().bootstrapModule(AppModule);
        });
}
