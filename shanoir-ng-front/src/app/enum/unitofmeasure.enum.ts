import {allOfEnum} from "../utils/app.utils";
import {Option} from "../shared/select/select.component";

export enum UnitOfMeasure {

    MS = "MS",

    PERCENT = "PERCENT",

    DEGREES = "DEGREES",

    G = "G",

    GY = "GY",

    HZ_PX = "HZ_PX",

    KG = "KG",

    M = "M",

    MG = "MG",

    MG_ML = "MG_ML",

    MHZ = "MHZ",

    ML = "ML",

    MM = "MM",

    PX = "PX",

    TESLA = "TESLA",

    KEV = "KEV",

    SEC = "S",

    MBQ = "MBQ",

    HZ = "HZ"

} export namespace UnitOfMeasure {

    export function all(): UnitOfMeasure[] {
        return allOfEnum<UnitOfMeasure>(UnitOfMeasure);
    }

    export function getLabelByKey(key: string){
        return this.getLabel(UnitOfMeasure[key]);
    }

    export function getLabel(type: UnitOfMeasure): string {
        if (!type) return
        switch (type) {
            case UnitOfMeasure.MS:
                return "ms";
                break;
            case UnitOfMeasure.SEC:
                return "s";
                break;
            case UnitOfMeasure.PERCENT:
                return "%";
                break;
            case UnitOfMeasure.DEGREES:
                return "°";
                break;
            case UnitOfMeasure.G:
                return "g";
                break;
            case UnitOfMeasure.KG:
                return "kg";
                break;
            case UnitOfMeasure.GY:
                return "Gy";
                break;
            case UnitOfMeasure.HZ_PX:
                return getLabel(UnitOfMeasure.HZ) + "/" + getLabel(UnitOfMeasure.PX);
                break;
            case UnitOfMeasure.M:
                return "m";
                break;
            case UnitOfMeasure.MG:
                return "mg";
                break;
            case UnitOfMeasure.MG_ML:
                return getLabel(UnitOfMeasure.MG) + "/" + getLabel(UnitOfMeasure.ML);
                break;
            case UnitOfMeasure.MHZ:
                return "MHz";
                break;
            case UnitOfMeasure.ML:
                return "ml";
                break;
            case UnitOfMeasure.MM:
                return "mm";
                break;
            case UnitOfMeasure.PX:
                return "px";
                break;
            case UnitOfMeasure.TESLA:
                return "T";
                break;
            case UnitOfMeasure.KEV:
                return "keV";
                break;
            case UnitOfMeasure.MBQ:
                return "MBq";
                break;
            case UnitOfMeasure.HZ:
                return "Hz";
                break;

        }
    }

    export var options: Option<UnitOfMeasure>[] = all().map(prop => new Option<UnitOfMeasure>(prop, getLabel(prop)));
}
