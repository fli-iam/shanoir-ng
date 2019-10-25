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

class Selenium_util:
    
    def __init__(self, driverToUse):
        global driver
        driver = driverToUse
    
    def check_exists_by_xpath(self, xpath):
        try:
            driver.find_element_by_xpath(xpath)
        except NoSuchElementException:
            return False
        return True
    
    def wait_to_be_present_and_click(self, xpath):
        WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, xpath)))
        driver.find_element_by_xpath(xpath).click()
        return True
    
    def wait_to_be_clickable_and_click(self, xpath):
        WebDriverWait(driver, 10).until(EC.element_to_be_clickable((By.XPATH, xpath)))
        driver.find_element_by_xpath(xpath).click()
        return True
    
    def wait_and_send_keys(self, xpath, text):
        WebDriverWait(driver, 10).until(EC.presence_of_element_located((By.XPATH, xpath)))
        driver.find_element_by_xpath(xpath).send_keys(text)
        return True
    
    def wait_to_be_visible(self, xpath):
        WebDriverWait(driver, 10).until(EC.visibility_of_element_located((By.XPATH, xpath)))
        return True
    
    def clear_input(self, xpath):
        WebDriverWait(driver, 10).until(EC.visibility_of_element_located((By.XPATH, xpath)))
        driver.find_element_by_xpath(xpath).clear()
        return driver.find_element_by_xpath(xpath).get_attribute("value") == ""
    
    def get_innertext(self, xpath):
        # TODO: doesn't work in firefox
        return driver.find_element_by_xpath(xpath).get_attribute("innerText")
    
    def count_elements(self, xpath):
        return len(driver.find_elements_by_xpath(xpath))
    
    def check_for_text_in_elements(self, text, xpath):
        for i in driver.find_elements_by_xpath(xpath):
            if text in i.get_attribute("innerText"):
                return True
        return False