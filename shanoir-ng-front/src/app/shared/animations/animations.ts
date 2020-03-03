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

import { animate, query, style, transition, trigger } from '@angular/animations';

export const parent = trigger('parent', [
    transition(':enter', [])
]);

export const slideDown = trigger('slideDown', [
    transition(
        ':enter', [
            style({height: '0', 'padding-bottom': '0', overflow: 'hidden'}),
            animate('500ms ease-in-out', style({height: '*', 'padding-bottom': '*', overflow: 'hidden'}))
        ]
    ),
    transition(
        ':leave', [
            style({height: '*', 'padding-bottom': '*', overflow: 'hidden'}),
            animate('500ms ease-in-out', style({height: '0', 'padding-bottom': '0', overflow: 'hidden'}))
        ]
    )
]);

export const slideRight = trigger('slideRight', [
    transition(
        ':enter', [
            style({width: 0}),
            animate('500ms ease-in-out', style({width: '*'}))
        ]
    ),
    transition(
        ':leave', [
            style({width: '*'}),
            animate('500ms ease-in-out', style({width: 0}))
        ]
    )
]);

export const slideMarginLeft = trigger('slideMarginLeft', [
    transition(
        'margin => nomargin', [
            style({'margin-left': 300}),
            animate('500ms ease-in-out', style({'margin-left': 0}))
        ]
    ),
    transition(
        'nomargin => margin', [
            style({'margin-left': 0}),
            animate('500ms ease-in-out', style({'margin-left': 300}))
        ]
    )
]);

export const menuAnimDur = 100;
export const menuSlideDown = trigger('menuSlideDown', [
    transition(
        ':enter', [
            style({ height: 0 }),
            animate(menuAnimDur + 'ms ease-in-out', style({ height: '*', 'padding-bottom': '*' }))
        ]
    ),
    transition(
        ':leave', [
            style({ height: '*' }),
            animate(menuAnimDur + 'ms ease-in-out', style({ height: 0, 'padding-bottom': '0' }))
        ]
    )
]);

export const menuSlideRight = trigger('menuSlideRight', [
    transition(
        ':enter', [
            style({width: 0}),
            animate(menuAnimDur+'ms ease-in-out', style({width: '*'}))
        ]
    ),
    transition(
        ':leave', [
            style({width: '*'}),
            animate(menuAnimDur+'ms ease-in-out', style({width: 0}))
        ]
    )
]);
    

export const preventInitialChildAnimations = trigger('preventInitialChildAnimations', [
    transition(
        ':enter', [
            query(':enter', [], {optional: true})
        ]
    )
]);


const moveDur = 1000;
export const moveDown = trigger('moveDown', [
    transition(
        ':enter', [
            style({height: '0', 'padding-bottom': '0', overflow: 'hidden'}),
            animate(moveDur + 'ms ease-in-out', style({height: '*', 'padding-bottom': '*', overflow: 'hidden'}))
        ]
    ),
    transition(
        ':leave', [
            style({height: '*', 'padding-bottom': '*', overflow: 'hidden'}),
            animate(moveDur + 'ms ease-in-out', style({height: '0', 'padding-bottom': '0', overflow: 'hidden'}))
        ]
    )
]);


export const moveUp = trigger('moveUp', [
    transition(
        ':enter', [
            style({height: '0', 'padding-bottom': '0', overflow: 'hidden'}),
            animate(moveDur + 'ms ease-in-out', style({height: '*', 'padding-bottom': '*', overflow: 'hidden'}))
        ]
    ),
    transition(
        ':leave', [
            style({height: '*', 'padding-bottom': '*', overflow: 'hidden'}),
            animate(moveDur + 'ms ease-in-out', style({height: '0', 'padding-bottom': '0', overflow: 'hidden'}))
        ]
    )
]);