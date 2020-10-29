# coding=utf8
# the above tag defines encoding for this document and is for Python 2.x compatibility

# errorFix.py enables to find & fix 'private attribute' errors by parsing the angular error logs 
# and replacing 'protected|private attributeName' by 'public attributeName' in conserned .ts files.

import re
import fileinput
from termcolor import colored

regex = r"Property '(?P<property>\w+)' is (?P<type>private|protected) and only accessible within class '(?P<component>\w+)'([\w\W\s\n]*?)src/app(?P<file>[\w\/\.\-]+)"

file = open('errors3.txt',mode='r')
test_str = file.read()
file.close()

matches = re.finditer(regex, test_str, re.MULTILINE)

for matchNum, match in enumerate(matches, start=1):
    
    print ( colored("\n\n\n\nMatch {matchNum} was found at {start}-{end}: {match}".format(matchNum = matchNum, start = match.start(), end = match.end(), match = match.group()), 'green') )
    
    # for groupNum in range(0, len(match.groups())):
    #     groupNum = groupNum + 1
        
    #     print ("Group {groupNum} found at {start}-{end}: {group}".format(groupNum = groupNum, start = match.start(groupNum), end = match.end(groupNum), group = match.group(groupNum)))

    # filename = '../shanoir-ng/shanoir-ng-front/src/app' + match.group('file')
    filename = '../src/app' + match.group('file')
    text_to_search = match.group('type') + ' get ' + match.group('property') + '('
    replacement_text =  'public get ' + match.group('property') + '('
    # print('text_to_search', text_to_search, replacement_text)

    with open(filename) as f:
        lines = f.readlines()
        text_found = False
        for line in lines:
            if text_to_search in line:
                print( colored( (filename, line.replace(text_to_search, replacement_text) ), 'yellow') )
                text_found = True
        if text_found:
            filedata = ''
            with open(filename, 'r') as file :
                filedata = file.read()

            filedata = filedata.replace(text_to_search, replacement_text)

            with open(filename, 'w') as file:
                file.write(filedata)
