/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */



// import 'buffer';

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { KeycloakService } from './app/shared/keycloak/keycloak.service';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import localeDe from '@angular/common/locales/de';
import localeEs from '@angular/common/locales/es';

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
