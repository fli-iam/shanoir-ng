// PRECLINICAL Rest Service
export const PRECLINICAL_API_ROOT_URL:string = process.env.BACKEND_API_PRECLINICAL_MS_URL;

//Subjects http api
export const PRECLINICAL_API_SUBJECTS_URL:string = PRECLINICAL_API_ROOT_URL + '/subject';
export const PRECLINICAL_API_SUBJECT_FIND_URL:string = PRECLINICAL_API_ROOT_URL + '/subject/find';
export const PRECLINICAL_API_SUBJECTS_ALL_URL:string = PRECLINICAL_API_SUBJECTS_URL + '/all';

//References http api
export const PRECLINICAL_API_REFERENCES_URL:string = PRECLINICAL_API_ROOT_URL + '/refs';
export const PRECLINICAL_API_REFERENCES_ALL_URL:string = PRECLINICAL_API_REFERENCES_URL + '/all';
export const PRECLINICAL_API_REFERENCES_CATEGORIES_ALL_URL:string = PRECLINICAL_API_REFERENCES_URL + '/categories';
export const PRECLINICAL_API_REF_CATEGORY_URL:string = PRECLINICAL_API_REFERENCES_URL + '/category';

//Pathologies http api
export const PRECLINICAL_API_PATHOLOGIES_URL:string = PRECLINICAL_API_ROOT_URL + '/pathology';
export const PRECLINICAL_API_PATHOLOGIES_ALL_URL:string = PRECLINICAL_API_PATHOLOGIES_URL + '/all';
export const PRECLINICAL_API_PATHOLOGY_MODELS_URL:string = PRECLINICAL_API_PATHOLOGIES_URL + '/model';
export const PRECLINICAL_API_PATHOLOGY_MODELS_ALL_URL:string = PRECLINICAL_API_PATHOLOGY_MODELS_URL + '/all';
export const PRECLINICAL_PATHOLOGY = 'pathology';

//Therapies http api
export const PRECLINICAL_API_THERAPIES_URL:string = PRECLINICAL_API_ROOT_URL + '/therapy';
export const PRECLINICAL_API_THERAPIES_ALL_URL:string = PRECLINICAL_API_THERAPIES_URL + '/all';
export const PRECLINICAL_THERAPY = 'therapy';

//Anesthetics http api
export const PRECLINICAL_API_ANESTHETICS_URL:string = PRECLINICAL_API_ROOT_URL + '/anesthetic';
export const PRECLINICAL_API_ANESTHETICS_ALL_URL:string = PRECLINICAL_API_ANESTHETICS_URL + '/all';
export const PRECLINICAL_API_ANESTHETIC_INGREDIENT_URL:string = '/ingredient';
export const PRECLINICAL_ANESTHETIC_INGREDIENT:string = 'ingredient';
export const PRECLINICAL_ANESTHETIC = 'anesthetic';

//Contrast agents http api
export const PRECLINICAL_API_CONTRAST_AGENTS_URL:string = PRECLINICAL_API_ROOT_URL + '/contrastagent';
export const PRECLINICAL_API_CONTRAST_AGENTS_ALL_URL:string = PRECLINICAL_API_CONTRAST_AGENTS_URL + '/all';
export const PRECLINICAL_API_CONTRAST_AGENTS_NAME_URL:string = PRECLINICAL_API_CONTRAST_AGENTS_URL + '/name';


export const PRECLINICAL_API_EXAMINATION_URL:string = PRECLINICAL_API_ROOT_URL + '/examination';
export const PRECLINICAL_API_PROTOCOL_URL:string = PRECLINICAL_API_ROOT_URL + '/protocol';

export const PRECLINICAL_API_EXTRA_DATA_PATH:string = PRECLINICAL_API_ROOT_URL + '/extradata';
export const PRECLINICAL_API_EXTRA_DATA_PHYSIO_PATH:string = PRECLINICAL_API_ROOT_URL + '/physiologicaldata';
export const PRECLINICAL_API_EXTRA_DATA_BLOODGAS_PATH:string = PRECLINICAL_API_ROOT_URL + '/bloodgasdata';
//export const PRECLINICAL_API_EXTRA_DATA_UPLOAD_PATH:string = PRECLINICAL_API_EXTRA_DATA_PATH + '/upload';
export const PRECLINICAL_API_EXTRA_DATA_UPLOAD_PATH:string = PRECLINICAL_API_ROOT_URL + '/upload/extradata/upload';


export const REL_PATH_SHANOIRNG:string = '../../../shanoirng/';
export const PRECLINICAL_ALL_URL = '/all';
export const PRECLINICAL_UPLOAD_URL = '/upload';

export const PRECLINICAL_CONTRASTAGENT_DATA = 'contrastagent';
export const PRECLINICAL_MODEL_DATA = 'model';
export const PRECLINICAL_EXTRA_DATA = 'extradata';
export const PRECLINICAL_PHYSIO_DATA = 'physiologicaldata';
export const PRECLINICAL_BLOODGAS_DATA = 'bloodgasdata';
//References
export const PRECLINICAL_CAT_SUBJECT = 'subject';
export const PRECLINICAL_SUBJECT_SPECIE = 'specie';
export const PRECLINICAL_SUBJECT_BIOTYPE = 'biotype';
export const PRECLINICAL_SUBJECT_STRAIN = 'strain';
export const PRECLINICAL_SUBJECT_PROVIDER = 'provider';
export const PRECLINICAL_SUBJECT_STABULATION = 'stabulation';

export const PRECLINICAL_CAT_ANATOMY = 'anatomy';
export const PRECLINICAL_ANATOMY_LOCATION = 'location';

export const PRECLINICAL_CAT_UNIT = 'unit';
export const PRECLINICAL_UNIT_VOLUME = 'volume';
export const PRECLINICAL_UNIT_CONCENTRATION = 'concentration';
export const PRECLINICAL_UNIT_GRAY = 'gray';
export const PRECLINICAL_UNIT_WEIGHT = 'weight';
export const PRECLINICAL_UNIT_TIME = 'time';

export const PRECLINICAL_CAT_CONTRAST_AGENT = 'contrastagent';
export const PRECLINICAL_CONTRAST_AGENT_NAME = 'name';

//BRUKER api
export const PRECLINICAL_API_BRUKER_UPLOAD:string = PRECLINICAL_API_ROOT_URL + '/bruker/upload';

