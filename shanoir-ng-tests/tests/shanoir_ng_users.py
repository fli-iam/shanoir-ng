import time
import os
import argparse
from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
from selenium.webdriver.common.action_chains import ActionChains
import random


parser = argparse.ArgumentParser()
parser.add_argument('-b', '--browser', type=str, help='Browser name:firefox, chrome (ie for local only)', required=True)
parser.add_argument('-a', '--address', type=str, help='Shanoir address: ex. \'http://localhost\'  for local or \
                    http://172.18.0.3 etc for docker', required=True)
parser.add_argument('--remote', action='store_true', help='Launch in docker')
parser.add_argument('-u', '--user', type=str, help='User login')
parser.add_argument('-p', '--password', type=str, help='User password')
args = parser.parse_args()
print args


def start_selenium():
    global driver
    b = args.browser
    if args.remote:
        if b.lower() == "chrome":
            dc = DesiredCapabilities.CHROME
        else:
            dc = DesiredCapabilities.FIREFOX
            dc['marionette'] = True
        driver = webdriver.Remote(command_executor='http://127.0.0.1:4444/wd/hub', desired_capabilities=dc)
        driver.get(args.address)
    else:
        if b.lower() == "ie":
            driver = webdriver.Ie(os.getcwd()+"\IEDriverServer.exe")
        elif b.lower() == "chrome":
            driver = webdriver.Chrome(os.getcwd()+"\chromedriver.exe")
        else:
            # Firefox
            fp = webdriver.FirefoxProfile()
            fp.set_preference("browser.startup.homepage_override.mstone", "ignore")
            fp.set_preference("startup.homepage_welcome_url.additional", "about:blank")
            driver = webdriver.Firefox(firefox_profile=fp)
        driver.get(args.address)
    # driver.set_window_size(1360, 1020)
    driver.maximize_window()


def login(user, password):
    # Enter login and password and submit
    input_login_xpath = "//input[@ng-reflect-name='email']"
    input_password_xpath = "//input[@ng-reflect-name='password']"
    button_login_xpath = "//button[contains(., 'Login')]"

    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, input_login_xpath)))
    driver.find_element_by_xpath(input_login_xpath).send_keys(user)
    driver.find_element_by_xpath(input_password_xpath).send_keys(password)

    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, button_login_xpath)))
    time.sleep(1)
    driver.find_element_by_xpath(button_login_xpath).click()

    # Click on Administration
    button_admin_xpath = "//span[contains(.,'Administration')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_admin_xpath)))
    ActionChains(driver).move_to_element(driver.find_element_by_xpath(button_admin_xpath)).perform()
    time.sleep(1)

    # Click on userlist
    button_userlist_xpath = "//a[@href='/userlist']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_userlist_xpath)))
    driver.find_element_by_xpath(button_userlist_xpath).click()


def add_user():
    random_int = random.randint(1000, 9999)
    role = "User"
    first_name = "test1"
    last_name = "test2"
    username = "testusername"+str(random_int)
    email = "testusername"+str(random_int)+"@shanoir.fr"
    expiration_date = "2017-05-05"

    button_add_user_xpath = "//a[@href='/editUser']"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, button_add_user_xpath)))
    driver.find_element_by_xpath(button_add_user_xpath).click()

    first_name_xpath = "//input[@id='firstName']"
    last_name_xpath = "//input[@id='lastName']"
    username_xpath = "//input[@id='username']"
    email_xpath = "//input[@id='email']"
    expiration_date_xpath = "//input[@id='expirationDate']"
    option_role_xpath = "//select[@id='role']/option[contains(.,'"+role+"')]"

    # Fill in the fields
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, option_role_xpath)))
    driver.find_element_by_xpath(first_name_xpath).send_keys(first_name)
    driver.find_element_by_xpath(last_name_xpath).send_keys(last_name)
    driver.find_element_by_xpath(username_xpath).send_keys(username)
    driver.find_element_by_xpath(email_xpath).send_keys(email)
    driver.find_element_by_xpath(expiration_date_xpath).send_keys(expiration_date)
    driver.find_element_by_xpath(option_role_xpath).click()

    # Submit
    submit_xpath = "//button[@type='submit']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, submit_xpath)))
    driver.find_element_by_xpath(submit_xpath).click()

    # Wait for the users table and find new user
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, "//div[@class='UserListComponent']")))

    # Scroll
    # time.sleep(2)
    # driver.execute_script("var h = document.getElementsByClassName('ag-body-container')[0].clientHeight;\
    #                       document.getElementsByClassName('ag-body-viewport')[0].scrollBy(0,h)")

    # Sort
    span_sort_by_id_xpath = '//span[@class="ag-header-cell-text"]'
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, span_sort_by_id_xpath)))
    driver.find_element_by_xpath(span_sort_by_id_xpath).click()
    driver.find_element_by_xpath(span_sort_by_id_xpath).click()

    # Click on row with user's email
    div_email_xpath = "//div[contains(.,'"+email+"') and @colid='email']"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, div_email_xpath)))
    driver.find_element_by_xpath(div_email_xpath).click()


def logout():
    # Click on Logout
    button_logout_xpath = "//button[contains(.,'Logout')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_logout_xpath)))
    driver.find_element_by_xpath(button_logout_xpath).click()


if __name__ == "__main__":
    start_selenium()
    login(args.user, args.password)
    add_user()
    time.sleep(60)
    logout()
    time.sleep(300)
    driver.quit()

