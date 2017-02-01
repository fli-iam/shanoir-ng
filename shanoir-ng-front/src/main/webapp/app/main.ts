import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app.module';
import { KeycloakService } from './keycloak/keycloak.service';

KeycloakService.init()
  .then(() => {
    const platform = platformBrowserDynamic();
    platform.bootstrapModule(AppModule);
  })
  .catch((err) => console.log(err));
//.catch(() => window.location.reload());