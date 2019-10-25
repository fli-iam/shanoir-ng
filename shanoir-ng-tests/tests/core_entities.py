import shanoir_util

def test_shanoir_core_entities(driver, shanoir_util_to_use):
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
    
    # Coil
    fields = [{
        'name': 'serialNb',
        'value': '12345',
        'valueEdited': '54321',
        'type': 'text',
        'label': 'Serial number'
    }, {
        'name': 'name',
        'value': 'laser optique',
        'valueEdited': 'loupe atomique',
        'type': 'text',
        'label': 'Name'
    }, {
        'name': 'center',
        'value': 'CHU Rennes',
        'valueEdited': 'CHU Reims',
        'type': 'select',
        'label': 'Center'
    }, {
        'name': 'acquiEquipModel',
        'value': 'Verio 3T (MR) - SIEMENS',
        'valueEdited': 'Artis Q  (MR) - SIEMENS',
        'type': 'select',
        'label': 'Aquisition Equipment Model'
    }, {
        'name': 'coilType',
        'value': 'HEAD',
        'valueEdited': 'BODY',
        'type': 'select',
        'label': 'Coil Type'
    }, {
        'name': 'nbChannel',
        'value': '38',
        'valueEdited': '77',
        'type': 'text',
        'label': 'Number of channels'
    }]
    menu = ['Manage data', 'Coil']
    shanoir_util_to_use.test_shanoir_crud_entity(menu, fields)
    
    # Acquisition equipment
    fields = [{
        'name': 'serialNumber',
        'value': '12345',
        'valueEdited': '54321',
        'type': 'text',
        'label': 'Serial number'
    }, {
        'name': 'manufacturerModel',
        'value': 'Verio 3T (MR) - SIEMENS',
        'valueEdited': 'Artis Q  (MR) - SIEMENS',
        'type': 'select',
        'label': 'Manufacturer'
    }, {
        'name': 'center',
        'value': 'CHU Rennes',
        'valueEdited': 'CHU Reims',
        'type': 'select',
        'label': 'Center'
    }]
    menu = ['Manage data', 'Acquisition equipment']
    shanoir_util_to_use.test_shanoir_crud_entity(menu, fields)

    # Study ?
    fields = [{
        'name': 'name',
        'value': 'Etude de cas sos',
        'valueEdited': 'Etude de cas rambar',
        'type': 'text',
        'label': 'Name'
    }, {
        'name': 'studyStatus',
        'value': 'Finished',
        'valueEdited': 'In Progress',
        'type': 'select',
        'label': 'Status'
    }, {
        'name': 'endDate',
        'value': '08/10/2019',
        'valueEdited': '18/10/2019',
        'type': 'date',
        'label': 'End date'
    }, {
        'name': 'center',
        'value': 'CHU Rennes',
        'valueEdited': 'CHU Reims',
        'type': 'select',
        'label': 'Center'
    }]
    menu = ['Manage data', 'Research study']
    shanoir_util_to_use.test_shanoir_crud_entity(menu, fields)