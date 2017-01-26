import { Component } from '@angular/core';
import { LoginService } from 'app/shared/login/login.service';

@Component({
    selector: 'navbar',
    moduleId: module.id,
    templateUrl: 'navbar.component.html',
    styleUrls: ['../css/common.css', 'navbar.component.css'],
})

export class NavbarComponent {


    constructor(private loginService: LoginService) {
    }

    isUserAdmin(): boolean {
        return this.loginService.isUserAdmin();
    }

    adminMenuOpen: boolean = false;

    public disabled:boolean = false;
    public status:{isopen:boolean} = {isopen: false};
    public items:Array<string> = ['The first choice!', 'And another choice for you.', 'but wait! A third!'];

    toggleAdminMenu() {
    	  this.adminMenuOpen = !this.adminMenuOpen;

    }

    public toggled(open:boolean):void {
        console.log('Dropdown is now: ', open);
    }
  
    public toggleDropdown($event:MouseEvent):void {
        $event.preventDefault();
        $event.stopPropagation();
        this.status.isopen = !this.status.isopen;
    }

    logout(event): void {
        event.preventDefault();
        this.loginService.logout();
    }

}