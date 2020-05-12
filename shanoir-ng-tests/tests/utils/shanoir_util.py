import time
import os
import argparse
from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.common.exceptions import NoSuchElementException        
import random
import traceback
import selenium_util


class Shanoir_util:
    def __init__(self, driverToSet, selenium_util_to_set):
        global selenium_utility
        global driver
        selenium_utility = selenium_util_to_set
        driver = driverToSet
        
    def login(self, user, password):
        selenium_utility.wait_and_send_keys("//input[@id='username']", user)
        selenium_utility.wait_and_send_keys("//input[@id='password']", password)
    
        time.sleep(1)
        selenium_utility.wait_to_be_clickable_and_click("//button[@name='login']")
    
        welcome_xpath = "//div[@class='header-component']//span[@class='welcome']"
        selenium_utility.wait_to_be_visible(welcome_xpath)
        time.sleep(1)
        assert "Welcome" in selenium_utility.get_innertext(welcome_xpath)
    
    def logout(self):
        selenium_utility.wait_to_be_clickable_and_click("//button[contains(.,'Logout')]")
        selenium_utility.wait_to_be_visible("//input[@id='username']")
        assert selenium_utility.count_elements("//div[contains(.,'Connect to Shanoir')]") > 0
    
    def check_if_shanoir_table_has_rows(self):
        table_xpath = "//shanoir-table//tr"
        selenium_utility.wait_to_be_visible(table_xpath)
        return selenium_utility.count_elements(table_xpath) > 0
    
    def go_to_entity(self, menu, submenu):
        button_manage_data_xpath = "//span[contains(.,'"+ menu +"')]"
        selenium_utility.wait_to_be_clickable_and_click(button_manage_data_xpath)
    
        button_acq_eq_xpath = "//menu-item[@label='"+ submenu +"']"
        selenium_utility.wait_to_be_clickable_and_click(button_acq_eq_xpath)
    
    def search(self, search_string, select_option):
        time.sleep(1)
        input_search_xpath = "//shanoir-table-search/input[@type='text']"
        selenium_utility.wait_and_send_keys(input_search_xpath, search_string)
    
        option_role_xpath = "//shanoir-table-search//option[contains(.,'"+select_option+"')]"
        time.sleep(1)
        selenium_utility.wait_to_be_clickable_and_click(option_role_xpath)
    
        time.sleep(1)
    
        assert self.check_if_shanoir_table_has_rows()
        assert selenium_utility.check_for_text_in_elements(search_string, "//shanoir-table//tbody/tr")
    
    def clean_search(self):
        button_clean_xpath = "//button[@title='clear search']"
        selenium_utility.wait_to_be_clickable_and_click(button_clean_xpath)
        assert self.check_if_shanoir_table_has_rows()
    
    def delete(self, name):
        selenium_utility.wait_to_be_present_and_click("//shanoir-table//tr[td/span[contains(text(), '" + name + "')]]//span/span/span/*[(local-name()='svg') and (@data-icon='trash')]")
        assert selenium_utility.wait_to_be_visible("//confirm-dialog[div[contains(.,'Are you sure you want to delete')]]")
        time.sleep(1)
        selenium_utility.wait_to_be_clickable_and_click("//button[contains(.,'OK')]")
        time.sleep(1)
        assert self.check_if_shanoir_table_has_rows()
        
    def add_entity(self, fields):
        selenium_utility.wait_to_be_present_and_click("//span[contains(.,'New')]")
        for field in fields:
            print field
            time.sleep(1)
            if (field['type'] == 'select'):
                selenium_utility.wait_to_be_clickable_and_click("//select-box[@formcontrolname='"+field['name']+"']/div[@class='root']")
                selenium_utility.wait_to_be_clickable_and_click("//select-box[@formcontrolname='"+field['name']+"']//select-option//div[contains(.,'"+field['value']+"')]")
            elif (field['type'] == 'text'):
                input_xpath = "//input[@formcontrolname='"+field['name']+"']"
                selenium_utility.wait_and_send_keys(input_xpath, field['value'])
            elif (field['type'] == 'textarea'):
                input_xpath = "//textarea[@formcontrolname='"+field['name']+"']"
                selenium_utility.wait_and_send_keys(input_xpath, field['value'])
            elif (field['type'] == 'date'):
                input_xpath = "//datepicker[@formcontrolname='"+field['name']+"']//input"
                selenium_utility.wait_and_send_keys(input_xpath, field['value'])
        selenium_utility.wait_to_be_clickable_and_click("//button[contains(.,'Create')]")
    
    def edit_entity(self, fields):
        selenium_utility.wait_to_be_present_and_click("//tbody//span[contains(.,'" + fields[0]['value'] + "')]")
        selenium_utility.wait_to_be_clickable_and_click("//button[contains(.,'Edit')]")

        for field in fields:
            time.sleep(1)

            if (field['type'] == 'select'):
                selenium_utility.wait_to_be_clickable_and_click("//select-box[@formcontrolname='"+field['name']+"']/div[@class='root']")
                selenium_utility.wait_to_be_clickable_and_click("//select-box[@formcontrolname='"+field['name']+"']//select-option//div[contains(.,'"+field['valueEdited']+"')]")
            elif (field['type'] == 'text'):
                input_xpath = "//input[@formcontrolname='"+field['name']+"']"
                selenium_utility.clear_input(input_xpath)
                selenium_utility.wait_and_send_keys(input_xpath, field['valueEdited'])
            elif (field['type'] == 'textarea'):
                input_xpath = "//textarea[@formcontrolname='"+field['name']+"']"
                selenium_utility.clear_input(input_xpath)
                selenium_utility.wait_and_send_keys(input_xpath, field['valueEdited'])
            elif (field['type'] == 'date'):
                input_xpath = "//datepicker[@formcontrolname='"+field['name']+"']//input"
                selenium_utility.clear_input(input_xpath)
                selenium_utility.wait_and_send_keys(input_xpath, field['valueEdited'])
        selenium_utility.wait_to_be_clickable_and_click("//button[contains(.,'Update')]")
    
    
    def checkFields(self, fields):
        # Got to read the entity
        selenium_utility.wait_to_be_present_and_click("//tbody//span[contains(.,'" + fields[0]['value'] + "')]")
    
        time.sleep(1)
        # Check fields value
        for field in fields:
            xpathFieldToCheck =  "//form//*[contains(., '" + field['value'] + "')]"
            assert selenium_utility.check_exists_by_xpath(xpathFieldToCheck)
    
    def test_shanoir_crud_entity(self, menu, fields):
        # This method tests SCRUD of a given entity
        # Search Create Read Update Delete
        print('Start ' + menu[1] + ' test')
        
        #Go to page
        self.go_to_entity(menu[0], menu[1])
        
        #Create a new entity
        self.add_entity(fields)
        
        time.sleep(1)
        
        # Search the entity
        fieldReferenceLabel = fields[0]['label']
        fieldReferenceValue = fields[0]['value']
        self.go_to_entity(menu[0], menu[1])
        self.search(fieldReferenceValue, fieldReferenceLabel)
        
        # Check the entity validity with given fields
        self.checkFields(fields)
        
        # Edit the entity
        self.go_to_entity(menu[0], menu[1])
        self.search(fieldReferenceValue, fieldReferenceLabel)
        self.edit_entity(fields)
        
        time.sleep(1)
        
        #Re-search the entity with new name
        fieldReferenceValue = fields[0]['valueEdited']
        self.go_to_entity(menu[0], menu[1])
        self.search(fieldReferenceValue, fieldReferenceLabel)
        
        # Delete the entity
        self.delete(fieldReferenceValue)
        
    def get_max_id(self, col_id):
        assert selenium_utility.check_exists_by_xpath("//shanoir-table/thead//span[text()='" + col_id +"']")

        # Dataset is ordered by Id by default (Be careful -> specific code here for dataset table)
        if (self.check_if_shanoir_table_has_rows()):
            xpath = "//shanoir-table/tbody/tr[1]/td[1]/span"
            return driver.find_element_by_xpath(xpath).text
        return 0;
    