export class FooterState {

    constructor(
        public mode: "view" | "edit" | "create",
        public canModify: boolean,
        public valid: boolean = false,
    ) {}
}