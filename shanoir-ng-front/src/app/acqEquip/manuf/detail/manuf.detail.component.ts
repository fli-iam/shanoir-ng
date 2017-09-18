import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { Manufacturer } from '../../shared/manuf.model';
import { ManufacturerService } from '../../shared/manuf.service';
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";

@Component({
    selector: 'manufDetail',
    templateUrl: 'manuf.detail.component.html'
})

export class ManufacturerDetailComponent implements OnInit {
    
    private manuf: Manufacturer = new Manufacturer();
    public manufDetailForm: FormGroup;
    private manufId: number;
    public mode: "view" | "edit" | "create";
    @Input() modeFromManufModel: "view" | "edit" | "create";
    @Output() closing: EventEmitter<any> = new EventEmitter();
    private isNameUnique: Boolean = true;
    public canModify: Boolean = false;

    constructor (private route: ActivatedRoute, private router: Router,
        private manufService: ManufacturerService,   private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        if (this.modeFromManufModel) {this.mode = this.modeFromManufModel;}
        this.getManufacturer();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    getManufacturer(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let manufId = queryParams['id'];
                if (!this.modeFromManufModel) {
                    let mode = queryParams['mode'];
                    if (mode) {
                        this.mode = mode;
                    }
                }
                if (manufId && this.mode !== 'create') {
                    // view or edit mode
                    this.manufId = manufId;
                    return this.manufService.getManufacturer(manufId);
                } else { 
                    // create mode
                    return Observable.of<Manufacturer>();
                }
            })
            .subscribe((manuf: Manufacturer) => {
                this.manuf = manuf;
            });
    }   

    buildForm(): void {
        this.manufDetailForm = this.fb.group({
            'name': [this.manuf.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]]
        });
        this.manufDetailForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.manufDetailForm) { return; }
        const form = this.manufDetailForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'name': ''
    };

    back(manufId?: number): void {
        if (this.closing.observers.length > 0) {
            this.manuf = new Manufacturer();
            this.closing.emit(manufId);
        } else {
            this.location.back();
        }
    }

    edit(): void {
        this.router.navigate(['/manufDetail'], { queryParams: {id: this.manufId, mode: "edit"}});
    }

    create(): void {
        this.manuf = this.manufDetailForm.value;
        this.manufService.create(this.manuf)
        .subscribe((manuf) => {
            this.back(manuf.id);
        }, (err: String) => {
            if (err.indexOf("name should be unique") != -1) {
                this.isNameUnique = false;
            }
        });
    }

    update(): void {
        this.manuf = this.manufDetailForm.value;
        this.manufService.update(this.manufId, this.manuf)
        .subscribe((manuf) => {
            this.back();
        }, (err: String) => {
            if (err.indexOf("name should be unique") != -1) {
                this.isNameUnique = false;
            }
        });
    }
}