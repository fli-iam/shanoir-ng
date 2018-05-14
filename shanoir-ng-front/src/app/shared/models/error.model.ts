export class GuiError extends Error {
    constructor(public guiMsg: string, error: Error) {
        super(error.message);
        this.name = error.name;
        this.stack = error.stack;
    }
}