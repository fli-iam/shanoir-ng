"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var core_1 = require('@angular/core');
var forms_1 = require('@angular/forms');
var router_1 = require('@angular/router');
var login_service_1 = require('./login.service');
var account_events_service_1 = require('../account/account.events.service');
var LoginComponent = (function () {
    function LoginComponent(router, loginService, accountEventsService, fb) {
        var _this = this;
        this.router = router;
        this.loginService = loginService;
        this.loginForm = fb.group({
            'email': ['', forms_1.Validators.required],
            'password': ['', forms_1.Validators.required]
        });
        accountEventsService.subscribe(function (account) {
            if (!account.authenticated) {
                if (account.error) {
                    if (account.error.indexOf('BadCredentialsException') !== -1) {
                        _this.error = 'Username and/or password are invalid !';
                    }
                    else {
                        _this.error = account.error;
                    }
                }
            }
        });
    }
    LoginComponent.prototype.login = function () {
        var _this = this;
        this.loginService.login(this.loginForm.value.email, this.loginForm.value.password)
            .subscribe(function (account) {
            _this.account = account;
            console.log('Successfully logged', account);
            _this.router.navigate(['/home']);
        });
    };
    LoginComponent = __decorate([
        core_1.Component({
            selector: 'shanoir-login',
            templateUrl: './app/login/login.html'
        }), 
        __metadata('design:paramtypes', [router_1.Router, login_service_1.LoginService, account_events_service_1.AccountEventsService, forms_1.FormBuilder])
    ], LoginComponent);
    return LoginComponent;
}());
exports.LoginComponent = LoginComponent;
//# sourceMappingURL=login.component.js.map