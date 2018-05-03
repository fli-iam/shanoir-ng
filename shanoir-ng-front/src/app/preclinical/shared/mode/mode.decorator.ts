import { Modes } from './mode.enum';

export function ModesAware(constructor: Function) {
    constructor.prototype.Modes = Modes;
}