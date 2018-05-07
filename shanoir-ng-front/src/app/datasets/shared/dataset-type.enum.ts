export enum DatasetType {
    CALIBRATION = "Calibration",
    CT = "Ct",
    EEG = "Eeg",
    MEG = "Meg",
    MESH = "Mesh",
    MR = "Mr",
    PARAMETER_QUANTIFICATION = "ParameterQuantification",
    PET = "Pet",
    REGISTRATION = "Registration",
    SEGMENTATION = "Segmentation",
    SPECT = "Spect",
    STATISTICAL = "Statistical",
    TEMPLATE = "Template"
}
export namespace DatasetType {

    export function keys(): Array<string> {
        let keys: Array<string> = [];
        for (let key in DatasetType) {
            if (key != 'values' && key != 'keys' && key != 'keyValues')
            keys.push(key);
        }
        return keys;
    }
    
    export function values(): Array<string> {
        let values: Array<string> = [];
        for (let key in keys()) values.push(DatasetType[key]);
        return values;
    }
    
    export function keyValues(): Array<Object> {
        let items: Array<Object> = [];
        for (let key of keys()) {
            items.push({key: key, value: DatasetType[key]});
        }
        return items;
    }
}