import { Injectable, ApplicationRef, Injector } from '@angular/core';
import { Subject } from 'rxjs';

type msgType = 'error' | 'warn' | 'info';
class Message { constructor(public type: msgType, public txt: string, public duration: number) {} }
const ANIMATION_TRANSITION_DURATION = 500;
const MSG_DURATION = 5000;

@Injectable()
export class MsgBoxService {

    private opened: boolean = false;
    private messages: Message[] = [];
    private appRef: ApplicationRef;

    constructor(private injector: Injector) {
        setTimeout(() => this.appRef = this.injector.get(ApplicationRef));
     }

    public log(type: msgType, txt: string, duration: number = MSG_DURATION) {
        let message = new Message(type, txt, duration);
        this.messages.push(message);
        if (!this.opened) this.run();
    }

    private run() {
        if (this.messages.length == 0) {
            this.close();
            return;
        }
        this.open();
        setTimeout(() => {
            this.close();
            setTimeout(() => {
                this.messages.splice(0, 1);
                this.run();
            }, ANIMATION_TRANSITION_DURATION);
        }, this.messages[0].duration);
    }

    private open() {
        if (!this.opened) {
            this.opened = true;
            this.appRef.tick();
        }
    }

    private close() {
        if (this.opened) {
            this.opened = false;
            this.appRef.tick();
        }
    }

    public isOpened(): boolean {
        return this.opened;
    };

    public getMsg(): Message {
        if (this.messages.length > 0)
            return this.messages[0];
        else return null;
    }

}

