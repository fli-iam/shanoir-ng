def test_core_study(driver, shanoir_util_to_use):
    # Study
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