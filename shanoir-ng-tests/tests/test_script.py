print "test in docker"
from pyvirtualdisplay import Display
from selenium import webdriver

display = Display(visible=0, size=(1024, 768))
display.start()

driver= webdriver.Firefox()
driver.get("https://portal.fli-iam.irisa.fr")
driver.maximize_window()

driver.quit()
display.stop()

print "end"


