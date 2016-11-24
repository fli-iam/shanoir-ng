def StartSelenium():
	import selenium
	import time
	from selenium import webdriver
	from selenium.webdriver.common.desired_capabilities import DesiredCapabilities
	firefox_capabilities = DesiredCapabilities.FIREFOX
	firefox_capabilities['marionette'] = True
	firefox_capabilities['binary'] = '/usr/bin/firefox'
	global browser
	browser = webdriver.Firefox(capabilities=firefox_capabilities)
	browser.get('https://www.google.com')
	print "title", browser.title

print "start"
StartSelenium()
print "end"