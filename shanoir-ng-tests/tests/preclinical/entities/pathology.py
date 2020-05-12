def test_preclinical_pathology(driver, shanoir_util_to_use):

    #Pathology model
    pathology_fields = [{
        'name': 'name',
        'value': 'U123',
        'valueEdited': 'U987',
        'type': 'text',
        'label': 'Name'
    }, {
        'name': 'pathology',
        'value': 'Stroke',
        'valueEdited': 'Cancer',
        'type': 'select',
        'label': 'Pathology'
    }]
    pathology_menu = ['Manage data', 'Pathology Models']
    shanoir_util_to_use.test_shanoir_crud_entity(pathology_menu, pathology_fields)
