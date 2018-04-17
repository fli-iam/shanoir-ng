export enum SubjectType {
    HEALTHY_VOLUNTEER = "Healthy volunteer",
    PATIENT = "Patient",
    PHANTOM = "Phantom"
}
export namespace SubjectType {

    export function keys(): Array<string> {
        let keys: Array<string> = [];
        for (let key in SubjectType) {
            if (key != 'values' && key != 'keys' && key != 'keyValues')
            keys.push(key);
        }
        return keys;
    }
    
    export function values(): Array<string> {
        let values: Array<string> = [];
        for (let key in keys()) values.push(SubjectType[key]);
        return values;
    }
    
    export function keyValues(): Array<Object> {
        let items: Array<Object> = [];
        for (let key of keys()) {
            items.push({key: key, value: SubjectType[key]});
        }
        return items;
    }
}