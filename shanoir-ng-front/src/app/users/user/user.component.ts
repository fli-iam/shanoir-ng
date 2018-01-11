import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Location } from '@angular/common';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import { IMyDate, IMyDateModel, IMyInputFieldChanged, IMyOptions } from 'mydatepicker';

import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import { Role } from '../../roles/role.model';
import { RoleService } from '../../roles/role.service';
import { AccountRequestInfo } from '../account-request-info/account-request-info.model';

@Component({
    selector: 'user-detail',
    templateUrl: 'user.component.html',
    styleUrls: ['user.component.css']
})

export class UserComponent implements OnInit {
    @Input() requestAccountMode: boolean = false;
    @Output() closing = new EventEmitter();
    user: User = new User();
    userForm: FormGroup;
    roles: Role[];
    isEmailUnique: boolean = true;
    isDateValid: boolean = true;
    creationMode: boolean;
    userId: number;
    selectedDateNormal: IMyDate;
    accountRequestInfo: AccountRequestInfo;
    private accountRequestInfoValid: boolean = false;

    constructor(private router: Router, private location: Location, private route: ActivatedRoute,
        private userService: UserService, private roleService: RoleService, private fb: FormBuilder) {
    }

    getRoles(): void {
        this.roleService
            .getRoles()
            .then(roles => {
                this.roles = roles;
                this.getUser();
            })
            .catch((error) => {
                // TODO: display error
                console.log("error getting roles list!");
            });
    }

    getUser(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let userId = queryParams['id'];
                if (userId) {
                    this.creationMode = false;
                    this.userId = userId;
                    return this.userService.getUser(userId);
                } else {
                    this.creationMode = true;
                    return Observable.of<User>();
                }
            })
            .subscribe((user: User) => {
                user.role = this.getRoleById(user.role.id);
                this.user = user;
                if (user.extensionRequestDemand) {
                    this.user.expirationDate = user.extensionRequestInfo.extensionDate;
                }
                this.getDateToDatePicker(this.user);
                this.accountRequestInfo = this.user.accountRequestInfo;
            });
    }

    getOut(user: User = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(user);
        } else {
            this.location.back();
        }
    }

    cancelAccountRequest(): void {
        window.location.href = process.env.LOGOUT_REDIRECT_URL;
    }

    accept(): void {
        this.submit();
        this.userService.confirmAccountRequest(this.userId, this.user)
            .subscribe((user) => {
                this.getOut();
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    deny(): void {
        this.userService.denyAccountRequest(this.userId)
            .then(res => {
                this.getOut();
            })
            .catch((error) => {
                // TODO: display error
                //log.error("error deny account request!");
                console.log("error deny account request!");
            });
    }

    create(): void {
        this.submit();
        this.userService.create(this.user)
            .subscribe((user) => {
                this.getOut(user);
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    accountRequest(): void {
        this.submit();
        this.userService.requestAccount(this.user)
            .subscribe((res) => {
                this.getOut(res);
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                } else {
                    this.getOut();
                }
            });
    }

    update(): void {
        this.submit();
        this.userService.update(this.userId, this.user)
            .subscribe((user) => {
                this.getOut(user);
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
                }
            });
    }

    submit(): void {
        this.user = this.userForm.value;
        this.setDateFromDatePicker();
        this.user.accountRequestInfo = this.accountRequestInfo;
    }

    isuserFormValid(): boolean {
        if (this.userForm.valid && this.isDateValid) {
            if (this.requestAccountMode) {
                if (this.accountRequestInfoValid) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    ngOnInit(): void {
        if (this.requestAccountMode) {
            this.creationMode = true;
        } else {
            this.getRoles();
        }
        this.buildForm();
    }

    buildForm(): void {
        const emailRegex = '^[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\.[a-zA-Z0-9-]+)*$';
        let roleFC: FormControl;
        if (this.requestAccountMode) {
            roleFC = new FormControl(this.user.role);
        } else {
            roleFC = new FormControl(this.user.role, Validators.required);
        }
        this.userForm = this.fb.group({
            'firstName': [this.user.firstName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'lastName': [this.user.lastName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'username': new FormControl(this.user.username),
            'email': [this.user.email, [Validators.required, Validators.pattern(emailRegex)]],
            'expirationDate': [this.user.expirationDate],
            'extensionMotivation': [(this.user.extensionRequestInfo) ? this.user.extensionRequestInfo.extensionMotivation : ''],
            'role': roleFC,
            'canAccessToDicomAssociation': new FormControl('false')
        });

        this.userForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.userForm) { return; }
        const form = this.userForm;
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
        'firstName': '',
        'lastName': '',
        'email': '',
        'role': ''
    };

    private myDatePickerOptions: IMyOptions = {
        dateFormat: 'dd/mm/yyyy',
        height: '20px',
        width: '160px'
    };

    onDateChanged(event: IMyDateModel) {
        if (event.formatted !== '') {
            this.selectedDateNormal = event.date;
        }
    }

    onInputFieldChanged(event: IMyInputFieldChanged) {
        if (event.value !== '') {
            if (!event.valid) {
                this.isDateValid = false;
            } else {
                this.isDateValid = true;
            }
        } else {
            this.isDateValid = true;
            setTimeout(():void => this.selectedDateNormal = null);
        }
    }

    setDateFromDatePicker(): void {
        if (this.selectedDateNormal) {
            this.user.expirationDate = new Date(this.selectedDateNormal.year, this.selectedDateNormal.month - 1, 
                this.selectedDateNormal.day);
        } else {
            this.user.expirationDate = null;
        }
    }

    getDateToDatePicker(user: User): void {
        if (user && user.expirationDate && !isNaN(new Date(user.expirationDate).getTime())) {
            let expirationDate:Date = new Date(user.expirationDate);
            this.selectedDateNormal = {year: expirationDate.getFullYear(), month: expirationDate.getMonth() + 1, 
                day: expirationDate.getDate()};;
        }
    }

    getRoleById(id: number): Role {
        for (let role of this.roles) {
            if (id == role.id) {
                return role;
            }
        }
        return null;
    }

    saveARI(ari: AccountRequestInfo): void {
        this.accountRequestInfo = ari;
    }

    updateARIValid(ariValid: boolean): void {
        this.accountRequestInfoValid = ariValid;
    }

}