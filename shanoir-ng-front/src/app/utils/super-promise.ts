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

export class SuperPromise<T> implements Promise<T> {

    public resolve: (value: T | PromiseLike<T>) => void;
    public reject: (reason?: any) => void;
    private promise: Promise<T> = new Promise<T>((resolve, reject) => { 
        this.resolve = resolve;
        this.reject = reject
    });

    then<TResult1 = T, TResult2 = never>(onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | null | undefined, onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | null | undefined): Promise<TResult1 | TResult2> {
        return this.promise.then(onfulfilled, onrejected);
    }

    catch<TResult = never>(onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | null | undefined): Promise<T | TResult> {
        return this.promise.catch(onrejected);
    }

    finally(onfinally?: (() => void) | null | undefined): Promise<T> {
        return this.promise.finally(onfinally);
    }

    get [Symbol.toStringTag](): string {
        return this.promise[Symbol.toStringTag];
    }

    public static timeoutPromise(milliseconds?: number): Promise<void> {
        let superPromise: SuperPromise<void> = new SuperPromise();
        setTimeout(() => {
            superPromise.resolve();
        }, milliseconds);
        return superPromise;
    }

}