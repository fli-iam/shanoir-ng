#!/bin/bash

NC='\033[0m'
RED='\033[0;31m'
GREEN='\033[0;32m'
LGREEN='\033[0;92m'
YELLOW='\033[0;33m'
count=0

if [ "$1" ]; then 
    dir=$1; 
else 
    dir='../../'
fi

build_commented_header () {
    rm var/commented_tmp -f
    rm var/previous_commented_tmp -f
    if [ -n "$2" ]; then 
        echo "$2" >> var/commented_tmp
        echo "$2" >> var/previous_commented_tmp
    fi
    while IFS="" read -r p || [ -n "$p" ]; do
        echo "$1$p" >> var/commented_tmp
    done < license-header
    while IFS="" read -r p || [ -n "$p" ]; do
        echo "$1$p" >> var/previous_commented_tmp
    done < var/previous-license-header
    if [ -n "$3" ]; then 
        echo "$3" >> var/commented_tmp
        echo "$3" >> var/previous_commented_tmp 
    fi
}

add_header_to () {
    headerNbLines=($(wc -l < var/commented_tmp))
    previousHeaderNbLines=($(wc -l < var/previous_commented_tmp))
    for arg in "$@"; do
        echo -n "searching for $arg ..."
        files=$(find $dir -name "$arg" -not -path "*/node_modules/*" -not -path "*/dist/*" -not -path "*/target/*")
        echo ' [DONE]'
        for i in $files; do
            if head $i -n $headerNbLines | cmp -s ./var/commented_tmp - ; then
                echo -e "${YELLOW}$i ... [ALREADY UP-TO-DATE]${NC}"  
            elif head $i -n $previousHeaderNbLines | cmp -s ./var/previous_commented_tmp - ; then
                echo -ne "${GREEN}$i ...\n"
                cp var/previous_commented_tmp $i.new && tail -n +$(($previousHeaderNbLines+1)) $i >> $i.new && mv $i.new $i
                echo -e " [LICENSE UPDATED]${NC}"  
            elif grep -q Copyright $i; then
                echo -e "${RED}$i ... [UNKNOWN LICENSE]${NC}"  
            else
                echo -ne "${LGREEN}$i ..."
                cat var/commented_tmp $i > $i.new && mv $i.new $i
                echo -e " [SHANOIR LICENSE ADDED]${NC}"
                count=$(($count+1))
            fi
        done
    done
}


build_commented_header " * " "/**" " */"
add_header_to "*.java" "*.ts"

build_commented_header "# "
add_header_to "*.yml" "*.properties"

build_commented_header "-- "
add_header_to "*.sql"

build_commented_header "" "<!--" "-->"
add_header_to "*.html"

echo -e "\n$count license headers added"
rm -f var/commented_tmp
rm -f var/previous_commented_tmp
cp -f ./license-header ./var/previous-license-header