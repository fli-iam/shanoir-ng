export enum ImagedObjectCategory {
    PHANTOM,
    LIVING_HUMAN_BEING,
    HUMAN_CADAVER,
    ANATOMICAL_PIECE
} 
export namespace ImagedObjectCategory {

    export function keys(): Array<string> {
        let keys: Array<string> = [];
        for (let key in ImagedObjectCategory) {
            if (key != 'values' && key != 'keys' && key != 'keyValues' && isNaN(Number(key)))
            keys.push(key);
        }
        return keys;
    }
}