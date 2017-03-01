import time
import os
import argparse
import pyperclip
import docker
from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.by import By


parser = argparse.ArgumentParser()
parser.add_argument('-b', '--browser', type=str, help='Browser name:firefox, chrome', required=True,
                    choices=["firefox", "chrome"])
parser.add_argument('-a', '--address', type=str, help='Keycloak address, localhost etc', required=True)
parser.add_argument('-u', '--user', type=str, help='User login')
parser.add_argument('-p', '--password', type=str, help='User password')
parser.add_argument('-j', '--json', type=str, help='Path to json files/scripts')
args = parser.parse_args()


def start_selenium():
    global driver
    b = args.browser
    if b.lower() == "ie":
        driver = webdriver.Ie(os.getcwd()+"\IEDriverServer.exe")
    elif b.lower() == "chrome":
        print "Chromedriver bug #35: impossible to focus on element when sending keys"
        return
        # driver = webdriver.Chrome(os.getcwd()+"\chromedriver.exe")
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


def login(user, password):
    # Enter login and password and submit
    input_login_xpath = "//input[@id='username']"
    input_password_xpath = "//input[@id='password']"
    button_login_xpath = "//input[@value='Log in']"

    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, input_login_xpath)))
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, input_password_xpath)))
    driver.find_element_by_xpath(input_login_xpath).send_keys(user)
    driver.find_element_by_xpath(input_password_xpath).send_keys(password)

    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, button_login_xpath)))
    time.sleep(1)
    driver.find_element_by_xpath(button_login_xpath).click()


def delete_realm():
    delete_xpath = "//i[@id='removeRealm']"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, delete_xpath)))
    driver.find_element_by_xpath(delete_xpath).click()

    confirm_xpath = "//button[contains(.,'Delete')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, confirm_xpath)))
    driver.find_element_by_xpath(confirm_xpath).click()


def create_realm(n):
    realm_selector_xpath = "//div[@class='realm-selector']"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, realm_selector_xpath)))
    driver.find_element_by_xpath(realm_selector_xpath).click()

    add_realm_xpath = "//div[@class='realm-add']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, add_realm_xpath)))
    driver.find_element_by_xpath(add_realm_xpath).click()
    time.sleep(1)

    name_xpath = "//input[@id='name']"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, name_xpath)))
    driver.find_element_by_xpath(name_xpath).send_keys(n)
    time.sleep(1)

    save_xpath = "//button[contains(.,'Create')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, save_xpath)))
    driver.find_element_by_xpath(save_xpath).click()


def add_client(path_to_file):
    clients_link_xpath = "//a[contains(@href,'clients')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, clients_link_xpath)))
    driver.find_element_by_xpath(clients_link_xpath).click()
    time.sleep(1)

    create_client_xpath = "//a[@id='createClient']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, create_client_xpath)))
    driver.find_element_by_xpath(create_client_xpath).click()

    input_import_file_xpath = "//input[@id='import-file']"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, input_import_file_xpath)))
    driver.execute_script("document.getElementById('import-file').className = 'visible';")
    time.sleep(1)
    driver.find_element_by_xpath(input_import_file_xpath).send_keys(path_to_file)

    # client_id_xpath = "//input[@id='clientId']"
    # WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, client_id_xpath)))
    # driver.find_element_by_xpath(client_id_xpath).send_keys(name)
    time.sleep(1)

    save_xpath = "//button[@type='submit']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, save_xpath)))
    driver.find_element_by_xpath(save_xpath).click()
    time.sleep(1)


def copy_browser_flow():
    auth_xpath ="//a[contains(@href,'/authentication/flows')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, auth_xpath)))
    driver.find_element_by_xpath(auth_xpath).click()

    # TODO: manage the cases when Browser is not selected
    # option_browser_xpath = "//select[@ng-model='flow']/option[@label='Browser']"
    # WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, option_browser_xpath)))
    # driver.find_element_by_xpath(option_browser_xpath).click()

    button_copy_xpath = "//button[contains(.,'Copy')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, button_copy_xpath)))
    driver.find_element_by_xpath(button_copy_xpath).click()

    confirm_xpath = "//button[contains(.,'Ok')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, confirm_xpath)))
    driver.find_element_by_xpath(confirm_xpath).click()

    # TODO: manage the cases when Copy of browser is not selected
    # option_copy_browser_xpath = "//select[@ng-model='flow']/option[@label='Copy of browser']"
    # WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, option_copy_browser_xpath)))
    # driver.find_element_by_xpath(option_copy_browser_xpath).click()


def add_execution(path_to_script):
    add_execution_xpath = "//button[contains(.,'Add execution')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, add_execution_xpath)))
    driver.find_element_by_xpath(add_execution_xpath).click()

    provider_option_xpath = "//select[@ng-model='provider']/option[@label='Script']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, provider_option_xpath)))
    driver.find_element_by_xpath(provider_option_xpath).click()

    save_xpath = "//button[@type='submit' and contains(.,'Save')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, save_xpath)))
    driver.find_element_by_xpath(save_xpath).click()

    required_xpath = "//tr[td[contains(.,'Script')]]//input[@type='radio' and @value='REQUIRED']"
    action_xpath = "//tr[td[contains(.,'Script')]]//a[contains(.,'Actions')]"
    config_xpath = "//tr[td[contains(.,'Script')]]//a[contains(.,'Config')]"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, required_xpath)))
    driver.find_element_by_xpath(required_xpath).click()
    time.sleep(1)
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, action_xpath)))
    driver.find_element_by_xpath(action_xpath).click()
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, config_xpath)))
    driver.find_element_by_xpath(config_xpath).click()

    content = open(path_to_script, 'r').read()
    pyperclip.copy(content)

    textarea_xpath = "//div[@class='ace_content']"
    WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, textarea_xpath)))

    textarea = driver.find_elements_by_xpath(textarea_xpath)[2]
    textarea.click()
    time.sleep(1)
    textarea.send_keys(Keys.LEFT_CONTROL + "a")
    textarea.send_keys(Keys.DELETE)
    textarea.send_keys(Keys.LEFT_CONTROL, "v")
    time.sleep(1)

    save_xpath = "//button[contains(.,'Save')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, save_xpath)))
    driver.find_element_by_xpath(save_xpath).click()


def change_binding(n):
    auth_xpath = "//a[contains(@href,'/authentication/flows')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, auth_xpath)))
    driver.find_element_by_xpath(auth_xpath).click()

    link_binding_xpath = "//a[contains(.,'Bindings')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, link_binding_xpath)))
    driver.find_element_by_xpath(link_binding_xpath).click()

    option_name_xpath = "//select[@id='browser']/option[@label='"+n+"']"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, option_name_xpath)))
    driver.find_element_by_xpath(option_name_xpath).click()

    save_xpath = "//button[contains(.,'Save')]"
    WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, save_xpath)))
    driver.find_element_by_xpath(save_xpath).click()


def exec_docker():
    docker_client = docker.from_env()
    container_name = "shanoir-ng-users"
    container_users_list = [n for n in docker_client.containers.list() if n.name == container_name]
    if len(container_users_list) == 0:
        raise ValueError("No container named " + container_name)
    else:
        container_users = container_users_list[0]
        print container_users.exec_run("java -jar /shanoir-ng-keycloak-init.jar", stdout=True, stderr=True)


if __name__ == "__main__":
    if os.path.isdir(args.json):
        client_front = os.path.join(args.json, "shanoir-ng-front.json")
        client_users = os.path.join(args.json, "shanoir-ng-users.json")
        client_sh_old = os.path.join(args.json, "shanoir-old.json")
        script = os.path.join(args.json, "authentication_checkExpirationDate.js")
    else:
        raise ValueError("incorrect path")

    start_selenium()
    login(args.user, args.password)

    delete_realm()

    create_realm("shanoir-ng")
    add_client(client_front)
    add_client(client_users)
    add_client(client_sh_old)
    copy_browser_flow()
    add_execution(script)
    change_binding("Copy of browser")

    exec_docker()
