

// import 'buffer';

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { KeycloakService } from './app/shared/keycloak/keycloak.service';

if (environment.production) {
  enableProdMode();
}

if (window.location.href.endsWith('/account-request') || window.location.href.endsWith('/extension-request') ||  window.location.href.endsWith('/challenge-request')) {
  // Public URL
  platformBrowserDynamic().bootstrapModule(AppModule);
} else {
  KeycloakService.init()
    .then(() => {
      platformBrowserDynamic().bootstrapModule(AppModule);
    });
}