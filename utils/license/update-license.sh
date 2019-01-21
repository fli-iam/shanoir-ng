#!/bin/bash

NC='\033[0m'
RED='\033[0;31m'
GREEN='\033[0;32m'
LGREEN='\033[0;92m'
YELLOW='\033[0;33m'
newCount=0

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
    echo -e "" >> var/commented_tmp
    echo -e "" >> var/previous_commented_tmp 
}

add_header_to () {
    headerNbLines=($(wc -l < var/commented_tmp))
    previousHeaderNbLines=($(wc -l < var/previous_commented_tmp))
    imHead=$(< var/commented_tmp)
    imPrevHead=$(< var/previous_commented_tmp)
    for arg in "$@"; do
        echo -n "searching for $arg ..."
        files=$(find $dir -name "$arg" \
            -not -path "*/node_modules/*" \
            -not -path "*/dist/*" \
            -not -path "*/target/*" \ 
            -not -path "*/bin/*" \
            -not -path "*/webapp/*" \ 
            -not -path "*/mnt-dist/*" \
            -not -path "*/.mvn/*" \
            -not -path "*/src/assets/*")
        echo ' [DONE]'
        for file in $files; do
            imFile=$(< $file)
            echo -ne "$file ..."
            if [[ ${imFile} = *${imHead}* ]]; then 
                echo -e "${YELLOW} [ALREADY UP-TO-DATE]${NC}"
            elif [[ ${imFile} = *${imPrevHead}* ]]; then 
                echo -e "${GREEN} [TODO : UPDATE LICENSE]${NC}"
            elif grep -q Copyright $file; then
                echo -e "${RED}$file ... [UNKNOWN LICENSE]${NC}"  
            else
                xmlLine="$(grep -ni "<?xml" "$file" | cut -d : -f 1)"
                if [ "${file##*.}" = "xml" ] && [ ! -z $xmlLine ] && [ $xmlLine -gt "0" ]; then
                    tail -n +$xmlLine $file | head -n 1 >> $file.new \
                    && echo "" >> $file.new \
                    && cat var/commented_tmp >> $file.new \
                    && tail -n +$(($xmlLine+1)) $file >> $file.new \
                    && mv $file.new $file
                else
                    cat var/commented_tmp $file > $file.new && mv $file.new $file
                fi
                echo -e "${LGREEN} [SHANOIR LICENSE ADDED]${NC}"
                newCount=$(($newCount+1))
            fi
        done
    done
}


build_commented_header " * " "/**" " */"
add_header_to "*.java" "*.ts" "*.js"

build_commented_header "# "
add_header_to "*.yml" "*.yaml" "*.properties" "Dockerfile"

build_commented_header "-- "
add_header_to "*.sql"

build_commented_header "" "<!--" "-->"
add_header_to "*.html" "*.xml"

echo -e "\n$newCount license headers added"
rm -f var/commented_tmp
rm -f var/previous_commented_tmp
cp -f ./license-header ./var/previous-license-header