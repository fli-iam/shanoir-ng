import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
import localeEs from '@angular/common/locales/es';
import localeFr from '@angular/common/locales/fr';
import { enableProdMode } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';

import { AppModule } from './app/app.module';
import { KeycloakService } from './app/shared/keycloak/keycloak.service';
import { environment } from './environments/environment';

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
    bootstrapApplication(AppModule);
} else if (window.location.href.includes('/dua/edit')
        || window.location.href.includes('/dua/view')) {
    // Can be either public or private
    KeycloakService.init(true)
    .then(() => {
        bootstrapApplication(AppModule);
    });
} else {
    // private 
    KeycloakService.init()
        .then(() => {
            bootstrapApplication(AppModule);
        });
}
