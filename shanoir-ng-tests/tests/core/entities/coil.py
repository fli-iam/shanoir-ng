def test_core_coil(driver, shanoir_util_to_use):
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