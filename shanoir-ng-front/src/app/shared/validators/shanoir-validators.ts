import {
    AbstractControl,
    ValidationErrors,
    ValidatorFn,
    Validators as NgValidators,
    FormGroup,
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

            const phoneRegex = /^[\+]?\d+$/;

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
