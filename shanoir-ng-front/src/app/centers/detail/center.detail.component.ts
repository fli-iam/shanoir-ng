import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';

@Component({
    selector: 'centerDetail',
    templateUrl: 'center.detail.component.html'
})

export class CenterDetailComponent implements OnInit {
    
    private center: Center = new Center();
    private centerDetailForm: FormGroup;
    private centerId: number;
    

    constructor (private route: ActivatedRoute, private router: Router,
        private centerService: CenterService,   private fb: FormBuilder) {

    }

    ngOnInit(): void {
        this.getCenter();
        this.buildForm();
    }

    getCenter(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let centerId = queryParams['id'];
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
        this.router.navigate(['/centerlist']);
    }

}