import 'hammerjs';

// import 'buffer';

import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';
import { KeycloakService } from './app/shared/keycloak/keycloak.service';

if (environment.production) {
  enableProdMode();
}

if (window.location.href.endsWith('/account-request')) {
  // Public URL
  platformBrowserDynamic().bootstrapModule(AppModule).catch(err => console.error(err));
} else {
  KeycloakService.init()
    .then(() => {
      platformBrowserDynamic().bootstrapModule(AppModule);
    }).catch(err => console.error(err));
}