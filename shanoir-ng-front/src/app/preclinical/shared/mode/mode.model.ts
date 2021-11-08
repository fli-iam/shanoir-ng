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

import { Modes } from './mode.enum';

export class Mode {
    //state: "view" | "edit" | "create";
    state: Modes;
    
    
    constructor(){
        //default is view
        this.state = Modes.view;
    }
    
    setModeFromParameter(mode:string){
        switch(mode){
            case 'view':
                this.state = Modes.view;
                break;
            case 'edit':
                this.state = Modes.edit;
                break;
            case 'create':
                this.state = Modes.create;
                break;
            default:
                this.state = Modes.view;
                break;
        }
    }
    
    getMode():Modes{
        return this.state;
    }
    
    createMode(){
        this.state == Modes.create;
    }
    
    editMode(){
        this.state == Modes.edit;
    }
    
    viewMode(){
        this.state == Modes.view;
    }
    
    isViewMode(): boolean{
        if(this.state == Modes.view) return true;
        return false;
    }
    
    isEditMode(): boolean{
        if(this.state == Modes.edit) return true;
        return false;
    }
    
    isCreateMode(): boolean{
        if(this.state == Modes.create) return true;
        return false;
    }
    
    
    
}