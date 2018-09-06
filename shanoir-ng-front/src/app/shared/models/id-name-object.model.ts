export class IdNameObject {
    constructor(
        public id: number, 
        public name: string, 
        public selected: boolean = false
    ) {}

    public static sortAll(objects: IdNameObject[], on: 'id' | 'name'): void {
        objects.sort((inoA, inoB) => {

            // If the return number is negative, A will be shown before B.
            // If the return number is 0, A and B will remain in the same order as when they entered the loop.
            // If the return number is positive, B will be shown before A.

            return 0;

        });
    }
}