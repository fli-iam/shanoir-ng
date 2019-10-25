import shanoir_util

def test_shanoir_preclinical(driver, shanoir_util_to_use):

    # Therapies
    #Id/reference is always the first element, and must contain a valueEdited field
    therapyFields = [{
        'name': 'name',
        'value': 'opium',
        'valueEdited': 'valium',
        'type': 'text',
        'label': 'Name'
    }, {
        'name': 'therapyType',
        'value': 'Drug',
        'valueEdited': 'Surgery',
        'type': 'select',
        'label': 'Type'
    }]
    therapyMenu = ['Manage data', 'Therapies']
    shanoir_util_to_use.test_shanoir_crud_entity(therapyMenu, therapyFields)

    #Pathology model
    pathologyFields = [{
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
    pathologyMenu = ['Manage data', 'Pathology Models']
    shanoir_util_to_use.test_shanoir_crud_entity(pathologyMenu, pathologyFields)

    #Anesthetics, here comment is the "ID"
    anesFields = [{
        'name': 'comment',
        'value': 'triclorethylene',
        'valueEdited': 'lessive',
        'type': 'textarea',
        'label': 'Comment'
    }, {
        'name': 'anestheticType',
        'value': 'Injection',
        'valueEdited': 'Gas',
        'type': 'select',
        'label': 'Type'
    }]
    anesMenu = ['Manage data', 'Anesthetics']
    shanoir_util_to_use.test_shanoir_crud_entity(anesMenu, anesFields)