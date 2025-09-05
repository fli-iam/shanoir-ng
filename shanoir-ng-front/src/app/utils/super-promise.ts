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
    private current: {
        promise: Promise<T>,
        resolve: (value: T | PromiseLike<T>) => void,
        reject: (reason?: any) => void
    };

    private isCancelled = false;

    constructor() {
        this.current = this.makeNewPromise();
    }

    private makeNewPromise() {
        let resolve, reject;
        const promise = new Promise<T>((res, rej) => {
            resolve = res;
            reject = rej;
        });
        return { promise, resolve, reject };
    }

    resolve(value: T) {
        if (this.isCancelled) return;
        this.current.resolve(value);
        this.current = this.makeNewPromise();
    }

    reject(reason?: any) {
        if (this.isCancelled) return;
        this.current.reject(reason);
        this.current = this.makeNewPromise(); 
    }

    cancel() {
        if (!this.isCancelled) {
            this.isCancelled = true;
            const err = new Error("Promise cancelled");
            err.name = "AbortError";
            this.current.reject(err);
        }
    }

    then<TResult1 = T, TResult2 = never>(
        onfulfilled?: ((value: T) => TResult1 | PromiseLike<TResult1>) | null,
        onrejected?: ((reason: any) => TResult2 | PromiseLike<TResult2>) | null
    ): Promise<TResult1 | TResult2> {
        return this.current.promise.then(onfulfilled, onrejected);
    }

    catch<TResult = never>(
        onrejected?: ((reason: any) => TResult | PromiseLike<TResult>) | null
    ): Promise<T | TResult> {
        return this.current.promise.catch(onrejected);
    }

    finally(onfinally?: (() => void) | null): Promise<T> {
        return this.current.promise.finally(onfinally);
    }

    get [Symbol.toStringTag]() {
        return this.current.promise[Symbol.toStringTag];
    }

    public static timeoutPromise(milliseconds?: number): Promise<void> {
        let superPromise: SuperPromise<void> = new SuperPromise();
        setTimeout(() => {
            superPromise.resolve();
        }, milliseconds);
        return superPromise;
    }
}
