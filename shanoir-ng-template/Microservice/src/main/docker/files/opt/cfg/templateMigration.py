#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(host="mysql", user="shanoir", password="shanoir", database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost",user="shanoir", password="shanoir", database="shanoir_ng_template", charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Import template: start")
    
sourceCursor.execute("SELECT TEMPLATE_ID, DATA FROM TEMPLATE")

query = "INSERT INTO template (id, data) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import template: end")

sourceConn.close()
targetConn.close()
