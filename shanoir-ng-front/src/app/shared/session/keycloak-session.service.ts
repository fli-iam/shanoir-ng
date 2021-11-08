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

import { Injectable } from '@angular/core';
import { KeycloakService } from '../keycloak/keycloak.service';
import { KeycloakSession } from './keycloak-session.model';

@Injectable()
export class KeycloakSessionService {
    
    private _sessionId: string;

    constructor() {
        if (this.isAuthenticated()) {
            this.initSession(KeycloakService.auth.authz.sessionId, KeycloakService.auth.authz.refreshTokenParsed.exp);
        }
        this.cleanStorage();
    }

    private initSession(sessionId: string, expirationTS: number) {
        this._sessionId = sessionId;
        if (!localStorage.getItem('kcsession-' + this._sessionId)) {
            this.saveSessionObject(new KeycloakSession(sessionId, expirationTS));
        }
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth && KeycloakService.auth.authz;
    }

    cleanStorage() {
        Object.keys(localStorage).filter(key => key.startsWith('kcsession-')).forEach(key => {
            const kcSession: KeycloakSession = JSON.parse(localStorage.getItem(key));
            const nowTS: number = Math.floor(Date.now()/1000);
            if (kcSession.expirationTS < nowTS) {
                localStorage.removeItem(key);
            }
        });
    }

    private getSessionObject(): KeycloakSession {
        return JSON.parse(localStorage.getItem('kcsession-' + this._sessionId));
    }

    private saveSessionObject(session: KeycloakSession) {
        localStorage.setItem('kcsession-' + this._sessionId, JSON.stringify(session));
    }

    get hasBeenAskedDUA(): boolean {
        let session = this.getSessionObject();
        return session ? session.hadDUAAlert : false;
    }

    set hasBeenAskedDUA(value: boolean) {
        let session: KeycloakSession = this.getSessionObject();
        if (session) {
            session.hadDUAAlert = value;
            this.saveSessionObject(session);
        }
    }

}
