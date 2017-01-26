export class Account {
    username:string;
    authorities:Array<string>;
    authenticated = true;

    constructor(account?:{username:string, authorities:Array<string>}) {
        if(account) {
            this.username = account.username;
            this.authorities = account.authorities;
            this.authenticated = false;
        }
    }
    
}