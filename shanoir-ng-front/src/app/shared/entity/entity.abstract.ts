import { Component, Input } from '@angular/core';
import { Location } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Params } from '@angular/router';

export abstract class AbstractEntity {

    private mode: 'create' | 'edit' | 'view';
    
    constructor(
            private route: ActivatedRoute,
            private location: Location) {}

    back(): void {
        this.location.back();
    }

}