def test_core_center(driver, shanoir_util_to_use):
    # Id/reference is always the first element, and must contain a valueEdited field
    # Center
    fields = [{
        'name': 'name',
        'value': 'CHU Melun',
        'valueEdited': 'CHU Fontaine',
        'type': 'text',
        'label': 'Name'
    }, {
        'name': 'city',
        'value': 'Melun',
        'valueEdited': 'Prefailles',
        'type': 'text',
        'label': 'Town'
    }, {
        'name': 'country',
        'value': 'France',
        'valueEdited': 'Iles federees de Micronesie',
        'type': 'text',
        'label': 'Country'
    }]
    menu = ['Manage data', 'Center']
    shanoir_util_to_use.test_shanoir_crud_entity(menu, fields)