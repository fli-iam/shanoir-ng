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