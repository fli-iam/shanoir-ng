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

@Injectable()
export class SessionService {
    
    public sessionId: string;
    readonly REFRESH_INTERVAL = 5000;
    readonly DEATH_THRESHOLD = 20000;
    
    constructor() {
        this.sessionId = this.makeId(16);
        setInterval(() => {
            this.refresh();
        }, this.REFRESH_INTERVAL);
    }

    private refresh() {
        localStorage.setItem('activeSession-' + this.sessionId, Date.now().toString()); // update ts
        this.cleanSessions();
    }

    public cleanSessions() {
        Object.keys(localStorage).filter(key => key.startsWith('activeSession-')).forEach(key => {
            let lastHeartBeat: number = parseInt(localStorage.getItem(key));
            if ((Date.now() - lastHeartBeat) > this.DEATH_THRESHOLD) {
                localStorage.removeItem(key);
            }
        });
    }

    public isActive(sessionKey: string) {
        return !!localStorage.getItem('activeSession-' + sessionKey);
    }

    private makeId(length): string {
        let result = '';
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        const charactersLength = characters.length;
        let counter = 0;
        while (counter < length) {
          result += characters.charAt(Math.floor(Math.random() * charactersLength));
          counter += 1;
        }
        return result;
    }
}