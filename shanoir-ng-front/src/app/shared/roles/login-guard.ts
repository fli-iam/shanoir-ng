import {CanActivate, Router} from "@angular/router";
import {Injectable} from "@angular/core";
import {KeycloakService} from "../keycloak/keycloak.service";

@Injectable()
export class LoginGuard implements CanActivate {

  constructor(
    private router: Router) {
  }

  canActivate() {
    if(KeycloakService.auth.loggedIn != true) {
      return true;
    } else {
      this.router.navigate(['/home']);
      return false;
    }
  }
}
