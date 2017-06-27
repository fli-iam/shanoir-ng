#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

sourceConn = pymysql.connect(host=os.environ['SHANOIR_OLD_MYSQL_HOST'],user=os.environ['SHANOIR_OLD_MYSQL_USER'],password=os.environ['SHANOIR_OLD_MYSQL_PWD'], database="shanoirdb", charset="utf8")
targetConn = pymysql.connect(host="localhost",user=os.environ['MYSQL_USER'],password=os.environ['MYSQL_PASSWORD'], database=os.environ['MYSQL_DATABASE'], charset="utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

print("Import template: start")
    
sourceCursor.execute("SELECT DATA_ID, DATA FROM data")

query = "INSERT INTO template (id, data) VALUES (%s, %s)"

targetCursor.executemany(query, sourceCursor.fetchall())
targetConn.commit()

print("Import template: end")

sourceConn.close()
targetConn.close()
