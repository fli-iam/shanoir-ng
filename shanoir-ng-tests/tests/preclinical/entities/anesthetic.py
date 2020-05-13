def test_preclinical_anesthetic(driver, shanoir_util_to_use):

    #Anesthetics, here comment is the "ID"
    anes_fields = [{
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
    anes_menu = ['Manage data', 'Anesthetics']
    shanoir_util_to_use.test_shanoir_crud_entity(anes_menu, anes_fields)