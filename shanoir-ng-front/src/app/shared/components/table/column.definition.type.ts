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
export type ColumnDefinition = {
    /** the header for that column */ 
    headerName?: string,

    /** the row item's field displayed in this column, ie if the item is 'car' you can set 'owner' or 'owner.name' as field */ 
    field?: string,

    /** used if field gives nothing */
    defaultField?: string,

    /** default is string, progress should be included in [0, 1] */
    type?: 'string' | 'number' | 'boolean' | 'button' | 'link' | 'date' | 'progress',

    /** tells if the table is sorted by this column by default */
    defaultSortCol?: boolean,

    /** tells if the default sorting (the first click) for this column is ascending */
    defaultAsc?: boolean,

    /** when ordering by this columns has to be based on another field(s), ie: {field: 'equipment', orderBy: ['equipment.id']} */
    orderBy?: string[],

    /** disable the possibility of sorting the table based on that column */
    disableSorting?: boolean,

    /** disable the possibility of sorting the table based on that column */
    disableSearch?: boolean,

    /** default width of the column as a css representation (20px, 15%, ...) */
    width?: string,

    /** hide the column */
    hidden?: boolean,

    /** a list or a function that return a list of possible values for using a select box as an input for ths column in edit mode */
    possibleValues?: any[] | ((item: any) => any[]),

    /** tells if this field is an array */
    multi?: boolean,

    /** builds the route string that will be used when clicking a cell from this column */
    route?: (item: any) => string,

    /** custom function that should return the displayed value of data (= the item) for this column */
    cellRenderer?: ((params?: {data?: any}) => any),

    /** perform an action when clicking a button type cell */
    action?: (item: any) => void,

    /** condition for displaying a button */ 
    condition?: (item: any) => boolean,

    /** boolean true value icon representation or button icon. See https://fontawesome.com/icons/ */
    awesome?: `fa${string} fa${string}`,

    /** css color the the awesome icon */ 
    color?: string,

    /** boolean false value icon */
    awesomeFalse?: `fa${string} fa${string}`,

    /** css color the the awesome icon */ 
    colorFalse?: string,

    /** add a descrption when cursor stands still a few second over the column */
    tip?: string,

    /** is this field editable in edit mode ? */
    editable?: boolean | ((item: any) => boolean),

    /** field edition callback */
    onEdit?: (item: any, fieldValue: any) => void,
}