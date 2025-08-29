export const DATE_FORMAT_DISPLAY: Record<string, string> = {
    'en': 'mm/dd/yyyy',
    'fr': 'jj/mm/aaaa',
    'de': 'tt.mm.jjjj',
    'es': 'dd/mm/aaaa'
};

export const DATE_FORMAT: Record<string, string> = {
    'en': 'MM/dd/yyyy',
    'fr': 'dd/MM/yyyy',
    'de': 'dd/MM/yyyy',
    'es': 'dd/MM/yyyy'
};

export const BROWSER_LANGUAGE = navigator.language.slice(0, 2);

export const dateDisplay = DATE_FORMAT_DISPLAY[BROWSER_LANGUAGE] || 'jj/mm/aaaa';

export const dateFormat = DATE_FORMAT[BROWSER_LANGUAGE] || 'dd/MM/yyyy';
