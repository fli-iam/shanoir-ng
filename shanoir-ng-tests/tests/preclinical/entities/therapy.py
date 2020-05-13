def test_preclinical_therapy(driver, shanoir_util_to_use):

    # Therapies
    #Id/reference is always the first element, and must contain a valueEdited field
    therapy_fields = [{
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
    therapy_menu = ['Manage data', 'Therapies']
    shanoir_util_to_use.test_shanoir_crud_entity(therapy_menu, therapy_fields)
