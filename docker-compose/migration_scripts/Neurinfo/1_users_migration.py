#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import pymysql

conn = pymysql.connect(
        host        = os.environ.get("TGT_HOST")        or "localhost",
        user        = os.environ.get("TGT_USER")        or "user",
        password    = os.environ.get("TGT_PASSWORD")    or "password",
        database    = os.environ.get("TGT_DATABASE")    or "users",
        charset     = os.environ.get("TGT_CHARSET")     or "utf8")

cursor = conn.cursor()


print("Update users: start")
query = "UPDATE users SET role_id=3 WHERE role_id=2"
cursor.execute(query)
conn.commit()
print("Update users: end")


print("Update roles: start")
query = "DELETE FROM role WHERE id=2"
cursor.execute(query)
conn.commit()
print("Update roles: end")


conn.close()
