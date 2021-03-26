import time
import os
import argparse

from selenium import webdriver
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import NoSuchElementException

from core.users.users import test_shanoir_ng_users
from preclinical.entities.therapy import test_preclinical_therapy
from preclinical.entities.anesthetic import test_preclinical_anesthetic
from preclinical.entities.pathology import test_preclinical_pathology
from core.entities.coil import test_core_coil
from core.entities.center import test_core_center
from core.entities.acquisition_equipement import test_core_acquisition_equipement
from core.entities.study import test_core_study
from preclinical.imports.import_bruker import test_shanoir_import_dicom
from utils.shanoir_util import *
from utils.selenium_util import *
import random
import traceback

parser = argparse.ArgumentParser()
parser.add_argument('-b', '--browser', type=str, choices=['firefox', 'chrome', 'ie'], help='Browser name', required=True)
parser.add_argument('-a', '--address', type=str, help='Shanoir address: ex. \'http://localhost\'  for local or \
                    http://shanoir-ng-users  for docker', required=True)
parser.add_argument('--remote', action='store_true', help='Launch in docker')
parser.add_argument('-u', '--user', type=str, help='User login', required=True)
parser.add_argument('-p', '--password', type=str, help='User password', required=True)
parser.add_argument('-s', '--shanoir', type=str, choices=['users', 'preclinical', 'entities', 'import', 'all'], help='Shanoir to test', required=True)
args = parser.parse_args()

path_to_downloads = os.getcwd()+"\\downloads\\"
if not os.path.exists(path_to_downloads):
    os.makedirs(path_to_downloads)

def start_selenium():
    global driver
    b = args.browser
    if args.remote:
        if b == "chrome":
            dc = DesiredCapabilities.CHROME
            dc['ACCEPT_SSL_CERTS'] = True
        else:
            dc = DesiredCapabilities.FIREFOX
            dc['marionette'] = True
        driver = webdriver.Remote(command_executor='http://127.0.0.1:4444/wd/hub', desired_capabilities=dc)
        driver.get(args.address)
    else:
        if b == "ie":
            driver = webdriver.Ie(os.getcwd()+"\IEDriverServer.exe")
        elif b == "chrome":
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
    
if __name__ == "__main__":
    try:
        start_selenium()
        selenium_utility = Selenium_util(driver)
        shanoir_utility = Shanoir_util(driver, selenium_utility)
        if args.shanoir in ["users", "all"]:
            test_shanoir_ng_users(driver, shanoir_utility, selenium_utility, args.user, args.password)
            shanoir_utility.logout()
            print('Users: Success!')
        
        if args.shanoir in ["preclinical", "all"]:
            shanoir_utility.login(args.user, args.password)
            test_preclinical_anesthetic(driver, shanoir_utility)
            test_preclinical_therapy(driver, shanoir_utility)
            test_preclinical_pathology(driver, shanoir_utility)
            shanoir_utility.logout()
            print('Preclinical entities: Success!')
        
        if args.shanoir in ["entities", "all"]:
            shanoir_utility.login(args.user, args.password)
#             test_core_acquisition_equipement(driver, shanoir_utility)
#             test_core_center(driver, shanoir_utility)
#             test_core_coil(driver, shanoir_utility)
            test_core_study(driver, shanoir_utility)
            shanoir_utility.logout()
            print('Core entities: Success!')
        
        if args.shanoir in ["import", "all"]:
            shanoir_utility.login(args.user, args.password)
            test_shanoir_import_dicom(driver, shanoir_utility, selenium_utility)
            shanoir_utility.logout()
            print('Preclinical import: Success!')
            
    except Exception as e:
        print(traceback.format_exc(e))
    finally:
        time.sleep(1)
        print('closing...')
        driver.close()
        driver.quit()
    