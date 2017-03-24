import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { KeycloakService } from './app/shared/keycloak/keycloak.service';

// Enable production mode unless running locally
if (process.env.ENV === 'production') {
  enableProdMode();
}

if (window.location.href.endsWith('/accountRequest')) {
  // Public URL
  platformBrowserDynamic().bootstrapModule(AppModule);
} else {
  KeycloakService.init()
    .then(() => {
      platformBrowserDynamic().bootstrapModule(AppModule);
    })
    .catch((err) => console.log(err));
}
