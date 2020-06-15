import os
import time
import random

def test_shanoir_import_dicom(driver_to_use, shanoir_util_to_use, selenium_util_to_use):
    global driver
    global shanoir_util
    global selenium_util
    selenium_util = selenium_util_to_use
    driver  = driver_to_use
    shanoir_util = shanoir_util_to_use
    
    # Get max ID of dataset to be able to delete it later
    shanoir_util.go_to_entity("Manage data", "Dataset");
    id_max = shanoir_util.get_max_id("Id");
    
    # Go to import page
    shanoir_util.go_to_entity('Import data', 'From BRUKER')

    # Load file
    driver.execute_script("""document.querySelector("upload-file input[type='file']").removeAttribute("hidden");""")

    file_input = driver.find_element_by_xpath("//upload-file/input[@type='file']")
    absolute_file_path = os.path.abspath("./archives/brucker.zip")
    file_input.send_keys(absolute_file_path)
    
    # Force change event
    driver.execute_script("document.getElementsByTagName('input')[0].dispatchEvent(new Event('change'))");

    # Next page
    time.sleep(3)
    selenium_util.wait_to_be_clickable_and_click("//button[contains(., 'Next')]")
    
    # Load only one element
    selenium_util.wait_to_be_clickable_and_click("//div/node/div/node/div/node/div/input[@type='checkbox']");
    
    # Next page
    selenium_util.wait_to_be_clickable_and_click("//button[contains(., 'Next')]")

    # Create a new dataset
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an existing research study']//select-box")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an existing research study']//select-box//select-option//div[text() = 'NATIVE Divers']")

    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a center']//select-box")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a center']//select-box//select-option//div[text() = 'CHU Rennes']")
    
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an acquisition equipment']//select-box")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an acquisition equipment']//select-box//select-option//div[text() = 'SIEMENS - Verio 3T (MR) 40296 - CHU Rennes']")    
    
    # TODO: Create a new subject
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a subject']//select-box//span/*[(local-name()='svg') and (@data-icon='file')]")    

    # Maybe test here to add pathological / anesthetic
    random_int = random.randint(1, 100000)

    fields = [{
        'name': 'name',
        'value': 'test_preclinical_' + str(random_int),
        'type': 'text',
        'label': 'Name'
    }, {
        'name': 'specie',
        'value': 'Rat',
        'type': 'select',
        'label': 'Specie'
    }, {
        'name': 'strain',
        'value': 'Wistar',
        'type': 'select',
        'label': 'Strain'
    }, {
        'name': 'biotype',
        'value': 'Wild',
        'type': 'select',
        'label': 'Biological type'
    }, {
        'name': 'provider',
        'value': 'Janvier',
        'type': 'select',
        'label': 'Provider'
    }, {
        'name': 'stabulation',
        'value': 'Paris',
        'type': 'select',
        'label': 'Stabulation'
    }]

    shanoir_util.add_entity(fields, False)

    # TODO: Create a new examination
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an examination']//select-box//span/*[(local-name()='svg') and (@data-icon='file')]")

    random_year = random.randint(1000, 3000)

    examFields = [{
        'name': 'examinationDate',
        'value': '12/12/' + str(random_year),
        'type': 'date',
        'label': 'Examination date'
    },{
        'name': 'subjectWeight',
        'value': '10',
        'type': 'text',
        'label': 'Subject Weight'
    }]

    shanoir_util.add_entity(examFields, False)

    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a Nifti converter']//select-box")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a Nifti converter']//select-box//select-option//div[text() = 'dcm2niix']")    

    # Next page
    selenium_util.wait_to_be_clickable_and_click("//button[contains(., 'Next')]")

    # Finish import
    selenium_util.wait_to_be_clickable_and_click("//button[contains(., 'Import now')]")
    
    # Wait for the data to be imported
    time.sleep(2)
    
    # Check that the dataset is now present
    shanoir_util.go_to_entity("Manage data", "Dataset");
    shanoir_util.check_if_shanoir_table_has_rows();

    id_to_delete = shanoir_util.get_max_id("Id");

    # Delete every added dataset element
    while (int(id_max) < int(id_to_delete) and selenium_util.check_exists_by_xpath("//shanoir-table/tbody/tr[1]/td[1]/span[text() = '" + str(id_to_delete) + "']")): 
        print id_to_delete
        shanoir_util.delete(str(id_to_delete))
        id_to_delete = int(id_to_delete) - 1
    
    # Delete examination
    shanoir_util.go_to_entity("Manage data", "Preclinical Examinations")
    fieldReferenceValue = examFields[0]['value']
    shanoir_util.delete(fieldReferenceValue)

    # Search then delete subject
    shanoir_util.go_to_entity("Manage data", "Preclinical Subjects")
    fieldReferenceValue = fields[0]['value']
    shanoir_util.delete(fieldReferenceValue)
    
