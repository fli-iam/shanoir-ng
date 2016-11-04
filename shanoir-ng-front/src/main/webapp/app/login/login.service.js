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
var http_1 = require('@angular/http');
var router_1 = require('@angular/router');
var Rx_1 = require('rxjs/Rx');
require('rxjs/add/operator/map');
var account_1 = require('../account/account');
var account_events_service_1 = require('../account/account.events.service');
var AppUtils = require('../utils/app.utils');
var LoginService = (function () {
    function LoginService(http, router, accountEventsService) {
        this.http = http;
        this.router = router;
        this.accountEventsService = accountEventsService;
        this.accountEventsService = accountEventsService;
    }
    LoginService.prototype.login = function (login, password) {
        var _this = this;
        var headers = new http_1.Headers();
        headers.append('Content-Type', 'application/json');
        return this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_AUTHENTICATE_PATH, JSON.stringify({ login: login, password: password }), { headers: headers })
            .map(function (res) {
            localStorage.setItem(AppUtils.STORAGE_ACCOUNT_TOKEN, res.text());
            localStorage.setItem(AppUtils.STORAGE_TOKEN, res.json().token);
            var account = new account_1.Account(res.json());
            _this.sendLoginSuccess(account);
            return account;
        })
            .catch(function (error) {
            if (error.status === 401) {
                _this.accountEventsService.logout({ error: error.text() });
            }
            return Rx_1.Observable.throw(error.message || error || 'Authentication fails');
        });
    };
    LoginService.prototype.sendLoginSuccess = function (account) {
        if (!account) {
            account = new account_1.Account(JSON.parse(localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN)));
        }
        this.accountEventsService.loginSuccess(account);
    };
    LoginService.prototype.logout = function () {
        var _this = this;
        var headersLogout = new http_1.Headers();
        headersLogout.append('Content-Type', 'application/json');
        headersLogout.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        this.http.post(AppUtils.BACKEND_API_ROOT_URL + AppUtils.BACKEND_API_LOGOUT_PATH, "", new http_1.RequestOptions({ headers: headersLogout, withCredentials: true })).subscribe(function () {
            _this.accountEventsService.logout(new account_1.Account(JSON.parse(localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN))));
            _this.removeAccount();
            _this.router.navigate(['/login']);
        });
    };
    LoginService.prototype.test = function () {
        var _this = this;
        var headersTest = new http_1.Headers();
        headersTest.append('Content-Type', 'application/json');
        headersTest.append('x-auth-token', localStorage.getItem(AppUtils.STORAGE_TOKEN));
        this.http.get(AppUtils.BACKEND_API_ROOT_URL + "/user", new http_1.RequestOptions({ headers: headersTest, withCredentials: true })).subscribe(function (data) {
            console.log("data: " + data.text());
        }, function (error) {
            if (error.status === 401) {
                _this.accountEventsService.logout({ error: error.text() });
            }
        });
    };
    LoginService.prototype.removeAccount = function () {
        localStorage.removeItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
        localStorage.removeItem(AppUtils.STORAGE_TOKEN);
    };
    LoginService.prototype.isAuthenticated = function () {
        return !!localStorage.getItem(AppUtils.STORAGE_ACCOUNT_TOKEN);
    };
    LoginService = __decorate([
        core_1.Injectable(), 
        __metadata('design:paramtypes', [http_1.Http, router_1.Router, account_events_service_1.AccountEventsService])
    ], LoginService);
    return LoginService;
}());
exports.LoginService = LoginService;
//# sourceMappingURL=login.service.js.map