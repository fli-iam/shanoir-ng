import time
import os
import argparse
from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
import random


parser = argparse.ArgumentParser()
parser.add_argument('-b', '--browser', type=str, help='Browser name:firefox, chrome (ie for local only)', required=True)
parser.add_argument('-a', '--address', type=str, help='Shanoir address: ex. \'http://localhost\'  for local or \
                    http://shanoir-ng-users  for docker', required=True)
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
            dc = DesiredCapabilities.FIREFOX
            dc['marionette'] = True
            driver = webdriver.Firefox(firefox_profile=fp, capabilities=dc)
        driver.get(args.address)
    # driver.set_window_size(1360, 1020)
    driver.maximize_window()

path_to_downloads = os.getcwd()+"\\downloads\\"
if not os.path.exists(path_to_downloads):
    os.makedirs(path_to_downloads)


def login(user, password):
    # Enter login and password and submit
    input_login_xpath = "//input[@ng-reflect-name='email']"
    input_password_xpath = "//input[@ng-reflect-name='password']"
    button_login_xpath = "//button[contains(., 'Login')]"

    WebDriverWait(driver, 30).until(EC.presence_of_element_located((By.XPATH, input_login_xpath)))
    WebDriverWait(driver, 30).until(EC.presence_of_element_located((By.XPATH, input_password_xpath)))
    driver.find_element_by_xpath(input_login_xpath).send_keys(user)
    driver.find_element_by_xpath(input_password_xpath).send_keys(password)

    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, button_login_xpath)))
    time.sleep(1)
    driver.find_element_by_xpath(button_login_xpath).click()


def manage_users():
    # Click on Administration
    button_admin_xpath = "//span[contains(.,'Administration')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_admin_xpath)))
    driver.find_element_by_xpath(button_admin_xpath).click()

    # Click on Manage Users
    button_userlist_xpath = "//span[contains(.,'Manage users')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_userlist_xpath)))
    driver.find_element_by_xpath(button_userlist_xpath).click()


def pink_mode():
    # Get the best color
    button_admin_xpath = "//span[contains(.,'Administration')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_admin_xpath)))
    driver.find_element_by_xpath(button_admin_xpath).click()

    button_preferences_xpath = "//span[contains(.,'Preferences')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_preferences_xpath)))
    driver.find_element_by_xpath(button_preferences_xpath).click()

    button_pink_mode_xpath = "//span[contains(.,'Pink mode')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_pink_mode_xpath)))
    driver.find_element_by_xpath(button_pink_mode_xpath).click()
    driver.find_element_by_xpath(button_admin_xpath).click()


def search(search_string, select_option):
    input_search_xpath = "//span[@class='text-search']/input[contains(@class,'search-txt')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, input_search_xpath)))
    driver.find_element_by_xpath(input_search_xpath).send_keys(search_string)

    option_role_xpath = "//span[@class='text-search']//option[contains(.,'"+select_option+"')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, option_role_xpath)))
    driver.find_element_by_xpath(option_role_xpath).click()
    time.sleep(1)
    driver.save_screenshot(path_to_downloads+search_string+"_search.jpg")


def clean_search():
    button_clean_xpath = "//span[@class='text-search']/button"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_clean_xpath)))
    driver.find_element_by_xpath(button_clean_xpath).click()


def add_user():
    random_int = random.randint(1000, 9999)
    role = "User"
    first_name = "test1"
    last_name = "test2"
    email = "testusername"+str(random_int)+"@shanoir.fr"
    expiration_date = "2017-05-05"

    # Click on Add new user
    button_add_user_xpath = "//span[contains(.,'new user')]"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, button_add_user_xpath)))
    driver.find_element_by_xpath(button_add_user_xpath).click()

    first_name_xpath = "//input[@id='firstName']"
    last_name_xpath = "//input[@id='lastName']"
    email_xpath = "//input[@id='email']"
    expiration_date_xpath = "//input[@aria-label='Calendar input field']"
    option_role_xpath = "//select[@id='role']/option[contains(.,'" + role + "')]"

    # Fill in the fields
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, option_role_xpath)))

    # Bug - separate names and emails and send multiple times
    driver.find_element_by_xpath(first_name_xpath).send_keys(first_name[:2])
    driver.find_element_by_xpath(first_name_xpath).send_keys(first_name[2:])
    driver.find_element_by_xpath(last_name_xpath).send_keys(last_name[:2])
    driver.find_element_by_xpath(last_name_xpath).send_keys(last_name[2:])

    driver.find_element_by_xpath(email_xpath).send_keys(email[:email.index("@") + 2])
    driver.find_element_by_xpath(email_xpath).send_keys(email[email.index("@") + 2:-1])
    driver.find_element_by_xpath(email_xpath).send_keys(email[-1])
    time.sleep(1)
    driver.find_element_by_xpath(expiration_date_xpath).send_keys(expiration_date)
    driver.find_element_by_xpath(option_role_xpath).click()
    driver.save_screenshot(path_to_downloads+email+"_add.jpg")

    # Submit
    submit_xpath = "//button[@type='submit']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, submit_xpath)))
    time.sleep(1)
    driver.find_element_by_xpath(submit_xpath).click()

    return email


def edit_user(t):
    # Click on Edit button
    button_edit_xpath = "//tr[td[contains(.,'"+t+"')]]//a[contains(@href,'editUser')]"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, button_edit_xpath)))
    driver.find_element_by_xpath(button_edit_xpath).click()

    # Enter new email
    email_edited = "edit"
    email_xpath = "//input[@id='email']"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, email_xpath)))
    input_email = driver.find_element_by_xpath(email_xpath)
    # driver.execute_script("document.getElementById('email').value = '';")
    input_email.send_keys(email_edited)
    driver.save_screenshot(path_to_downloads + t + "_edit.jpg")

    # Submit
    submit_xpath = "//button[@type='submit']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, submit_xpath)))
    driver.find_element_by_xpath(submit_xpath).click()


def delete_user(t):
    # Click on Delete button
    button_delete_xpath = "//tr[td[contains(.,'"+t+"')]]//img[contains(@src,'delete')]"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, button_delete_xpath)))
    driver.find_element_by_xpath(button_delete_xpath).click()

    # Confirm
    button_confirm_xpath = "//button[contains(.,'OK')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_confirm_xpath)))
    driver.save_screenshot(path_to_downloads + t + "_delete.jpg")
    driver.find_element_by_xpath(button_confirm_xpath).click()


def request_account():
    link_create_account_xpath = "//a[contains(.,'Create an account')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, link_create_account_xpath)))
    driver.find_element_by_xpath(link_create_account_xpath).click()

    random_int = random.randint(1000, 9999)
    first_name = "test_account"
    last_name = "test_account"
    email = "testusername"+str(random_int)+"@shanoir.fr"

    request_inputs = "test"

    first_name_xpath = "//input[@id='firstName']"
    last_name_xpath = "//input[@id='lastName']"
    email_xpath = "//input[@id='email']"

    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, first_name_xpath)))

    # Bug - separate names and emails and send multiple times
    driver.find_element_by_xpath(first_name_xpath).send_keys(first_name[:2])
    driver.find_element_by_xpath(first_name_xpath).send_keys(first_name[2:])
    driver.find_element_by_xpath(last_name_xpath).send_keys(last_name[:2])
    driver.find_element_by_xpath(last_name_xpath).send_keys(last_name[2:])

    driver.find_element_by_xpath(email_xpath).send_keys(email[:email.index("@") + 2])
    driver.find_element_by_xpath(email_xpath).send_keys(email[email.index("@") + 2:-1])
    driver.find_element_by_xpath(email_xpath).send_keys(email[-1])

    driver.find_element_by_xpath("//input[@formcontrolname='contact']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='function']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='institution']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='service']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='study']").send_keys(request_inputs)
    driver.find_element_by_xpath("//input[@formcontrolname='work']").send_keys(request_inputs)

    time.sleep(1)

    # Submit
    submit_xpath = "//button[@type='submit']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, submit_xpath)))
    driver.find_element_by_xpath(submit_xpath).click()

    return email


def accept_deny_account_request(u, accept):
    # Check if is inactive
    span_od_xpath = "//tr[td[contains(.,'"+u+"')]]/td[contains(@class,'cell-accountRequestDemand')]//\
    span[contains(@class,'bool-true')]"
    span_class = driver.find_element_by_xpath(span_od_xpath).get_attribute('class')
    assert span_class == "bool-true"

    # Click on Edit button
    button_edit_xpath = "//tr[td[contains(.,'"+u+"')]]//a[contains(@href,'editUser')]"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, button_edit_xpath)))
    driver.find_element_by_xpath(button_edit_xpath).click()

    if accept:
        # Choose exp date and role
        role = "Expert"
        exp_date = "2017-05-05"
        expiration_date_xpath = "//input[@aria-label='Calendar input field']"
        WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, expiration_date_xpath)))
        driver.find_element_by_xpath(expiration_date_xpath).send_keys(exp_date)
        option_role_xpath = "//select[@id='role']/option[contains(.,'"+role+"')]"
        driver.find_element_by_xpath(option_role_xpath).click()

    driver.save_screenshot(path_to_downloads + u + "_request.jpg")

    if accept:
        button_accept_xpath = "//button[@type='submit' and contains(.,'Accept')]"
        WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_accept_xpath)))
        driver.find_element_by_xpath(button_accept_xpath).click()
    else:
        button_deny_xpath = "//button[@type='submit' and contains(.,'Deny creation')]"
        WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_deny_xpath)))
        driver.find_element_by_xpath(button_deny_xpath).click()


def logout():
    # Click on Logout
    button_logout_xpath = "//button[contains(.,'Logout')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_logout_xpath)))
    driver.find_element_by_xpath(button_logout_xpath).click()


if __name__ == "__main__":
    start_selenium()

    # Request 2 accounts
    acc1 = request_account()
    acc2 = request_account()

    # Login and go to Manage users
    login(args.user, args.password)
    pink_mode()
    manage_users()

    # Accept account request
    search(acc1, 'Email')
    accept_deny_account_request(acc1, True)
    search(acc1, 'Email')
    edit_user(acc1)
    search(acc1, 'Email')
    delete_user(acc1)
    clean_search()

    # Deny account request
    search(acc2, 'Email')
    accept_deny_account_request(acc2, False)

    # Create, edit and delete user
    email = add_user()
    search(email, 'Email')
    edit_user(email)
    search(email, 'Email')
    delete_user(email)
    clean_search()

    logout()
    driver.quit()

