/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import {
    AbstractControl,
    ValidationErrors,
    ValidatorFn,
    Validators as NgValidators,
} from '@angular/forms';

export class ShanoirValidators {
    // === Re-export all Angular native validators ===
    static min = NgValidators.min;
    static max = NgValidators.max;
    static required = NgValidators.required;
    static requiredTrue = NgValidators.requiredTrue;
    static email = NgValidators.email;
    static minLength = NgValidators.minLength;
    static maxLength = NgValidators.maxLength;
    static pattern = NgValidators.pattern;
    static nullValidator = NgValidators.nullValidator;
    static compose = NgValidators.compose;
    static composeAsync = NgValidators.composeAsync;

    /** Rejects values containing special characters */
    static noSpecialChars(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            return /[^a-zA-Z0-9]/.test(control.value) ? { specialChars: true } : null;
        };
    }

    /** Checks if number is even */
    static isPairNumber(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const value = Number(control.value);
            return isNaN(value) || value % 2 !== 0 ? { notEven: true } : null;
        };
    }

    /** Checks if it's phone number */
    static isPhoneNumber(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const value = control.value;
            if (!value) return null;

            const phoneRegex = /^[+]?\d+$/;

            return phoneRegex.test(value) ? null : { invalidPhoneNumber: true };
        };
    }


    /** Check if it's a mail address respecting usual mail rules */
    static isEmail(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            const value = control.value;
            if (!value) return null;

            const emailRegex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,}$/;

            return emailRegex.test(value.trim()) ? null : { invalidEmail: true };
        };
    }
}
