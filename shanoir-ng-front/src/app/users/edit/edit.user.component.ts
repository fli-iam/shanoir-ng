import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Location } from '@angular/common';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import { IMyOptions, IMyDateModel, IMyInputFieldChanged } from 'mydatepicker';

import { User } from '../shared/user.model';
import { UserService } from '../shared/user.service';
import { Role } from '../../roles/role.model';
import { RoleService } from '../../roles/role.service';
import { AccountRequestInfo } from '../accountRequestInfo/account.request.info.model';

@Component({
    selector: 'editUser',
    templateUrl: 'edit.user.component.html',
    styleUrls: ['edit.user.component.css']
})

export class EditUserComponent implements OnInit {
    @Input() requestAccountMode: boolean = false;
    @Output() closing = new EventEmitter();
    user: User = new User();
    editUserForm: FormGroup;
    roles: Role[];
    isEmailUnique: boolean = true;
    isDateValid: boolean = true;
    creationMode: boolean;
    userId: number;
    selectedDateNormal: string = '';
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
                //log.error("error getting roles list!");
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
        this.router.navigate(['/home']);
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
            .subscribe((user) => {
                window.location.href = process.env.LOGOUT_REDIRECT_URL;
            }, (err: String) => {
                if (err.indexOf("email should be unique") != -1) {
                    this.isEmailUnique = false;
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
        this.user = this.editUserForm.value;
        this.setDateFromDatePicker();
        this.user.accountRequestInfo = this.accountRequestInfo;
    }

    isEditUserFormValid(): boolean {
        if (this.editUserForm.valid && this.isDateValid) {
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
        this.editUserForm = this.fb.group({
            'firstName': [this.user.firstName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'lastName': [this.user.lastName, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
            'username': new FormControl(this.user.username),
            'email': [this.user.email, [Validators.required, Validators.pattern(emailRegex)]],
            'expirationDate': [this.user.expirationDate],
            'role': roleFC,
            'canAccessToDicomAssociation': new FormControl('false'),
            'medical': new FormControl('false')
        });

        this.editUserForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); // (re)set validation messages now
    }

    onValueChanged(data?: any) {
        if (!this.editUserForm) { return; }
        const form = this.editUserForm;
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
        dateFormat: 'yyyy-mm-dd',
        height: '20px',
        width: '160px'
    };

    onDateChanged(event: IMyDateModel) {
        if (event.formatted !== '') {
            this.selectedDateNormal = event.formatted;
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
            this.selectedDateNormal = null;
        }
    }

    setDateFromDatePicker(): void {
        if (this.selectedDateNormal && !isNaN(new Date(this.selectedDateNormal).getTime())) {
            this.user.expirationDate = new Date(this.selectedDateNormal);
        } else {
            this.user.expirationDate = null;
        }
    }

    getDateToDatePicker(user: User): void {
        if (user && user.expirationDate && !isNaN(new Date(user.expirationDate).getTime())) {
            let date: string = new Date(user.expirationDate).toISOString().split('T')[0];
            this.selectedDateNormal = date;
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