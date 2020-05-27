import os
import time

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
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an existing research study']//select")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an existing research study']//select/option[text() = 'NATIVE Divers']")

    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a center']//select")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a center']//select/option[text() = 'CHU Rennes']")
    
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an acquisition equipment']//select")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an acquisition equipment']//select/option[text() = 'SIEMENS - Verio 3T (MR) 40296 - CHU Rennes']")    
    
    # Create a new subject
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a subject']//select")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a subject']//select/option[text() = 'subject1']")    

    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an examination']//select")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select an examination']//select/option[contains(.,'examination1')]")    
 
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a Nifti converter']//select")
    selenium_util.wait_to_be_clickable_and_click("//ol/li[label/text() = 'Select a Nifti converter']//select/option[contains(.,'dcm2nii_2008-03-31')]")    

    # Next page
    selenium_util.wait_to_be_clickable_and_click("//button[contains(., 'Next')]")

    # Finish import
    selenium_util.wait_to_be_clickable_and_click("//button[contains(., 'Import now')]")
    
    # Wait for the data to be imported
    time.sleep(2)
    
    # Check that the dataset is now present
    shanoir_util.go_to_entity("Manage data", "Subject");
    shanoir_util.go_to_entity("Manage data", "Dataset");
    shanoir_util.check_if_shanoir_table_has_rows();

    driver.save_screenshot("screenshot.png")
    id_to_delete = shanoir_util.get_max_id("Id");

    # Delete every added dataset element
    while (int(id_max) < int(id_to_delete) and selenium_util.check_exists_by_xpath("//shanoir-table/tbody/tr[1]/td[1]/span[text() = '" + str(id_to_delete) + "']")): 
        print id_to_delete
        shanoir_util.delete(str(id_to_delete))
        id_to_delete = int(id_to_delete) - 1
    
