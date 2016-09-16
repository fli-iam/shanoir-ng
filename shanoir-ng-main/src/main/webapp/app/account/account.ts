export class Account {
    id:number;
    login:string;
    authorities:Array<string>;
    authenticated = true;
    constructor(account?:{id:number, login:string, authorities:Array<string>}) {
    }
}