import {allOfEnum} from "../utils/app.utils";
import {Option} from "../shared/select/select.component";

export enum UnitOfMeasure {

    MS = "ms",

    PERCENT = "%",

    DEGREES = "°",

    G = "g",

    GY = "Gy",

    HZ_PX = "Hz/px",

    KG = "kg",

    M = "m",

    MG = "mg",

    MG_ML = "mg/ml",

    MHZ = "MHz",

    ML = "ml",

    MM = "mm",

    PX = "px",

    TESLA = "T",

    KEV = "keV",

    SEC = "s",

    BQ = "Bq",

    HZ = "Hz"

} export namespace UnitOfMeasure {

    export function all(): Array<UnitOfMeasure> {
        return allOfEnum<UnitOfMeasure>(UnitOfMeasure);
    }

    export function getLabel(type: UnitOfMeasure): string {
        if (!type) return
        return type.replace(new RegExp('_', 'g'), '/').toLowerCase();
    }

    export var options: Option<UnitOfMeasure>[] = all().map(prop => new Option<UnitOfMeasure>(prop, getLabel(prop)));
}
