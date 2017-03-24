import { style, state, animate, transition, trigger } from '@angular/core';

export const slideDown = trigger('slideDown', [
    transition(
        ':enter', [
            style({height: '0', 'padding-bottom': '0'}),
            animate('500ms ease-in-out', style({height: '*', 'padding-bottom': '*'}))
        ]
    ),
    transition(
        ':leave', [
            style({height: '*', 'padding-bottom': '*'}),
            animate('500ms ease-in-out', style({height: 0, 'padding-bottom': '0'}))
        ]
    )
]);