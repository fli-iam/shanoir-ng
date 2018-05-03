export enum ImagedObjectCategory {
    PHANTOM = "Phantom",
    LIVING_HUMAN_BEING = "Living animal being",
    HUMAN_CADAVER = "Animal cadaver",
    ANATOMICAL_PIECE = "Anatomical piece"
} 
export namespace ImagedObjectCategory {

    export function keys(): Array<string> {
        let keys: Array<string> = [];
        for (let key in ImagedObjectCategory) {
            if (key != 'values' && key != 'keys' && key != 'keyValues')
            keys.push(key);
        }
        return keys;
    }
    
    export function values(): Array<string> {
        let values: Array<string> = [];
        for (let key in keys()) values.push(ImagedObjectCategory[key]);
        return values;
    }
    
    export function keyValues(): Array<Object> {
        let items: Array<Object> = [];
        for (let key of keys()) {
            items.push({key: key, value: ImagedObjectCategory[key]});
        }
        return items;
    }
}