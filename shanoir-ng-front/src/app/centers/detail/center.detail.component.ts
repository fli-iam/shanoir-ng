import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";

@Component({
    selector: 'centerDetail',
    templateUrl: 'center.detail.component.html',
    styleUrls: ['center.detail.component.css']
})

export class CenterDetailComponent implements OnInit {

    private center: Center = new Center();
    public centerDetailForm: FormGroup;
    private centerId: number;
    public mode: "view" | "edit" | "create";
    private isNameUnique: Boolean = true;
    public canModify: Boolean = false;
    private phoneNumberPatternError = false;

    constructor(private route: ActivatedRoute, private router: Router,
        private centerService: CenterService, private fb: FormBuilder,
        private location: Location, private keycloakService: KeycloakService) {

    }

    ngOnInit(): void {
        this.getCenter();
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    getCenter(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let centerId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode = mode;
                }
                if (centerId) {
                    // view or edit mode
                    this.centerId = centerId;
                    return this.centerService.getCenter(centerId);
                } else {
                    // create mode
                    return Observable.of<Center>();
                }
            })
            .subscribe((center: Center) => {
                this.center = center;
            });
    }

    buildForm(): void {
        this.centerDetailForm = this.fb.group({
            'name': [this.center.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200)]],
            'street': [this.center.street],
            'postalCode': [this.center.postalCode],
            'city': [this.center.city],
            'country': [this.center.country],
            'phoneNumber': [this.center.phoneNumber],
            'website': [this.center.website]
        });
        this.centerDetailForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.centerDetailForm) { return; }
        const form = this.centerDetailForm;
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

    back(): void {
        this.location.back();
    }

    edit(): void {
        this.router.navigate(['/centerDetail'], { queryParams: { id: this.centerId, mode: "edit" } });
    }

    create(): void {
        this.center = this.centerDetailForm.value;
        this.centerService.create(this.center)
            .subscribe((center) => {
                this.back();
            }, (err: string) => {
                this.manageRequestErrors(err);
            });
    }

    update(): void {
        this.center = this.centerDetailForm.value;
        this.centerService.update(this.centerId, this.center)
            .subscribe((center) => {
                this.back();
            }, (err: string) => {
                this.manageRequestErrors(err);
            });
    }

    private manageRequestErrors(err: string): void {
        if (err.indexOf("name should be unique") != -1) {
            this.isNameUnique = false;
        }
        if (err.indexOf("phoneNumber should be Pattern") != -1) {
            this.phoneNumberPatternError = true;
        }
    }

    resetNameErrorMsg(): void {
        this.isNameUnique = true;
    }

    resetPhoneNumberErrorMsg(): void {
        this.phoneNumberPatternError = false;
    }

}