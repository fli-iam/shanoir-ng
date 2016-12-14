import time
import os
import argparse
from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By


parser = argparse.ArgumentParser()
parser.add_argument('-b', '--browser', type=str, help='Browser name:firefox, chrome (ie for local only)', required=True)
parser.add_argument('-a', '--address', type=str, help='Shanoir address: ex. \'http://localhost\'  for local or \
                    http://172.18.0.3 for docker', required=True)
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


def login(user, password):
    input_login_xpath = "//input[@ng-reflect-name='email']"
    input_password_xpath = "//input[@ng-reflect-name='password']"
    button_submit_xpath = "//button[@type='submit']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, input_login_xpath)))
    driver.find_element_by_xpath(input_login_xpath).send_keys(user)
    driver.find_element_by_xpath(input_password_xpath).send_keys(password, Keys.ENTER)
    button_login_xpath = "//button[contains(.,'Login')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_login_xpath)))
    driver.find_element_by_xpath(button_login_xpath).click()
    button_logout_xpath = "//button[contains(.,'Logout')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_logout_xpath)))
    # driver.save_screenshot('screenie.png')


if __name__ == "__main__":
    start_selenium()
    login(args.user, args.password)
    print "title", driver.title
    print "end"

    driver.quit()

