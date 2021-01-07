// convertRoutes enables to generate static routes from dynamic ones (which were used with the older angular version).
// just run `node convertRoutes.ts`
 
function getRoutesFor(entityName, entityComponent, listComponent, auth) {
    
    return [
        {
            path: entityName,
            redirectTo: entityName + '/list',
            pathMatch: 'full',
        }, {
            path: entityName + '/list',
            component: listComponent,
            canActivate: auth.read ? [auth.read] : undefined,
        }, {
            path: entityName+'/details/:id',
            component: entityComponent,
            data: { mode: 'view' },
            canActivate: auth.read ? [auth.read] : undefined,
        }, {
            path: entityName+'/edit/:id',
            component: entityComponent,
            data: { mode: 'edit' },
            canActivate: auth.update ? [auth.update] : undefined,
        }, {
            path: entityName+'/create',
            component: entityComponent,
            data: { mode: 'create' },
            canActivate: auth.create ? [auth.create] : undefined,
        }
    ];
};

let routes = [];

routes = routes.concat(
    getRoutesFor('study', 'StudyComponent', 'StudyListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('subject', 'SubjectComponent', 'SubjectListComponent', {update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('examination', 'ExaminationComponent', 'ExaminationListComponent', {update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('dataset', 'DatasetComponent', 'DatasetListComponent', {update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('center', 'CenterComponent', 'CenterListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('acquisition-equipment', 'AcquisitionEquipmentComponent', 'AcquisitionEquipmentListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('coil', 'CoilComponent', 'CoilListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('user', 'UserComponent', 'UserListComponent', {create: 'AuthAdminGuard'}),
    getRoutesFor('manufacturer', 'ManufacturerComponent', null, {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('manufacturer-model', 'ManufacturerModelComponent', null, {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('study-card', 'StudyCardComponent', 'StudyCardListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('dataset-acquisition', 'DatasetAcquisitionComponent', 'DatasetAcquisitionListComponent', {update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('instrument', 'InstrumentAssessmentComponent', null, {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('preclinical-reference', 'ReferenceFormComponent', 'ReferencesListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}), 
    getRoutesFor('preclinical-examination', 'AnimalExaminationFormComponent', 'AnimalExaminationListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('preclinical-therapy', 'TherapyFormComponent', 'TherapiesListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('preclinical-pathology', 'PathologyFormComponent', 'PathologiesListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}), 
    getRoutesFor('preclinical-pathology-model', 'PathologyModelFormComponent', 'PathologyModelsListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('preclinical-anesthetic-ingredient', 'AnestheticIngredientFormComponent', 'AnestheticIngredientsListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('preclinical-anesthetic', 'AnestheticFormComponent', 'AnestheticsListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'}),
    getRoutesFor('preclinical-subject', 'AnimalSubjectFormComponent', 'AnimalSubjectsListComponent', {create: 'AuthAdminOrExpertGuard', update: 'AuthAdminOrExpertGuard'})
);

console.log(routes)

let output = '[\n';
for (let route of routes) {
    output += '\t{\n'
    output += '\t\tpath: \'' + route.path + '\',\n';
    if (route.redirectTo) {
        output += '\t\tredirectTo: \'' + route.redirectTo + '\',\n';
    }
    if (route.component) {
        output += '\t\tcomponent: ' + route.component + ',\n';
    }
    if (route.data) {
        output += '\t\tdata: { mode: \'' + route.data.mode + '\' },\n';
    }
    if (route.canActivate != undefined) {
       output += '\t\tcanActivate: [' + route.canActivate[0] + '],\n';
    }
    output += '\t},\n'
}

console.log(output)