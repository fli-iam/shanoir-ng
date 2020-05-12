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
from lib2to3.pgen2 import driver


def test_shanoir_ng_users(driverToUse,  shanoir_util_to_use, selenium_util_to_use, user, password):
    global driver
    global shanoir_util
    global selenium_util
    shanoir_util = shanoir_util_to_use
    selenium_util = selenium_util_to_use
    driver = driverToUse

    print('Start users test')

    # Request 2 accounts
    acc1 = request_account()
    time.sleep(1)
    acc2 = request_account()

    shanoir_util.login(user, password)
    shanoir_util.go_to_entity('Administration', 'Manage users')

    # Accept account request
    shanoir_util.search(search_string=acc1, select_option='Email')
    accept_deny_account_request(user=acc1, accept=True)
    time.sleep(1)

    shanoir_util.search(search_string=acc1, select_option='Email')
    edit_user(name=acc1)
    shanoir_util.clean_search()

    # Deny account request
    shanoir_util.search(search_string=acc2, select_option='Email')
    accept_deny_account_request(user=acc2, accept=False)

    # Create, edit and delete user
    email = add_user()
    time.sleep(1)

    shanoir_util.search(search_string=email, select_option='Email')
    edit_user(name=email)
    shanoir_util.go_to_entity('Administration', 'Manage users')
    shanoir_util.search(search_string=email, select_option='Email')
    shanoir_util.delete(email)
    shanoir_util.clean_search()

def add_user():
    random_int = random.randint(1000, 9999)
    role = "User"
    first_name = "test1"
    last_name = "test2"
    email = "testusername"+str(random_int)+"@shanoir.fr"
    expiration_date = "01/01/2025"

    selenium_util.wait_to_be_present_and_click("//span[contains(.,'New')]")

    first_name_xpath = "//input[@id='firstName']"
    last_name_xpath = "//input[@id='lastName']"
    email_xpath = "//input[@id='email']"
    expiration_date_xpath = "//input[@aria-label='Date input field']"
    option_role_xpath = "//select[@id='role']/option[contains(.,'" + role + "')]"

    # Fill in the fields
    selenium_util.wait_to_be_visible(option_role_xpath)

    driver.find_element_by_xpath(first_name_xpath).send_keys(first_name)
    driver.find_element_by_xpath(last_name_xpath).send_keys(last_name)
    driver.find_element_by_xpath(email_xpath).send_keys(email)
    time.sleep(1)
    driver.find_element_by_xpath(expiration_date_xpath).send_keys(expiration_date)
    driver.find_element_by_xpath(option_role_xpath).click()

    # Submit
    submit_xpath = "//button[@type='submit']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, submit_xpath)))
    time.sleep(1)
    driver.find_element_by_xpath(submit_xpath).click()
    
    time.sleep(1)
    cancelButton = "//button[contains(.,'Cancel')]"
    if (selenium_util.check_exists_by_xpath(cancelButton)):
        selenium_util.wait_to_be_clickable_and_click(cancelButton)

    return email

def edit_user(name):
    selenium_util.wait_to_be_present_and_click("//tr/td/span[contains(.,'"+name+"')]")
    selenium_util.wait_and_send_keys("//input[@id='email']", "edit")
    selenium_util.wait_to_be_clickable_and_click("//button[@type='submit']")

def request_account():
    link_create_account_xpath = "//a[contains(.,'Create an account')]"
    selenium_util.wait_to_be_clickable_and_click(link_create_account_xpath)

    random_int = random.randint(1000, 9999)
    first_name = "test_account"
    last_name = "test_account"
    email = "testusername"+str(random_int)+"@shanoir.fr"
    request_inputs = "test"

    first_name_xpath = "//input[@id='firstName']"
    last_name_xpath = "//input[@id='lastName']"
    email_xpath = "//input[@id='email']"

    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, first_name_xpath)))

    driver.find_element_by_xpath(first_name_xpath).send_keys(first_name)
    driver.find_element_by_xpath(last_name_xpath).send_keys(last_name)
    driver.find_element_by_xpath(email_xpath).send_keys(email)

    driver.find_element_by_xpath("//input[@formcontrolname='contact']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='function']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='institution']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='service']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='study']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='work']").send_keys(request_inputs)
    time.sleep(1)

    # Submit
    submit_xpath = "//button[@type='submit']"
    selenium_util.wait_to_be_clickable_and_click(submit_xpath)
    # Cancel
    cancel_xpath = "//button[contains(.,'Cancel')]"
    selenium_util.wait_to_be_clickable_and_click(cancel_xpath)
    return email


def accept_deny_account_request(user, accept):
    # Check if is inactive
    span_od_xpath = "//td[@title='On Demand']//*[local-name() = 'svg']"
    is_waiting = driver.find_element_by_xpath(span_od_xpath);
    is_waiting = is_waiting.get_attribute('data-icon')
    assert is_waiting == "check"

    # Click on Edit button
    button_edit_xpath = "//tr/td//*[@data-icon='edit']"
    selenium_util.wait_to_be_present_and_click(button_edit_xpath)
    selenium_util.wait_to_be_visible("//label[contains(.,'Institution')]")

    if accept:
        # Choose role
        role = "Expert"
        selenium_util.wait_to_be_present_and_click("//select[@id='role']/option[contains(.,'"+role+"')]")
        selenium_util.wait_to_be_clickable_and_click("//button[@type='submit' and contains(.,'Accept')]")
    else:
        selenium_util.wait_to_be_clickable_and_click("//button[@type='submit' and contains(.,'Deny creation')]")
    
    cancelButton = "//button[contains(.,'Cancel')]"
    if (selenium_util.check_exists_by_xpath(cancelButton)):
        selenium_util.wait_to_be_clickable_and_click(cancelButton)