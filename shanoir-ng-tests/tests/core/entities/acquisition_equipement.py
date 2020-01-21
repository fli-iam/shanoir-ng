def test_core_acquisition_equipement(driver, shanoir_util_to_use):
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