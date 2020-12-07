#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

conn = pymysql.connect(host="localhost", user="shanoir", password="shanoir", database="users", charset="utf8")

cursor = conn.cursor()

print("Update users: start")

query = "UPDATE users SET role_id=3 WHERE role_id=2"

cursor.executemany(query, users)
conn.commit()

print("Update users: end")

print("Update roles: start")

query = "DELETE FROM role WHERE id=2"

cursor.execute(query)
conn.commit()

print("Update roles: end")

conn.close()
