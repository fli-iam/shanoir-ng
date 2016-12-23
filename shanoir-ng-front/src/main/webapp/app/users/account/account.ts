export class Account {
    id:number;
    login:string;
    authorities:Array<string>;
    authenticated = true;

    constructor(account?:{id:number, login:string, authorities:Array<string>}) {
        if(account) {
            this.id = account.id;
            this.login = account.login;
            this.authorities = account.authorities;
            this.authenticated = false;
        }
    }
    
}