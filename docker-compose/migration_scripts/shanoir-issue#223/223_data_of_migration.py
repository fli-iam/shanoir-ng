import itertools
import os
import pymysql

from dbutils import bulk_insert

sourceConn = pymysql.connect(
        host        = os.environ.get("SRC_HOST")        or "localhost",
        user        = os.environ.get("SRC_USER")        or "root",
        password    = os.environ.get("SRC_PASSWORD")    or "",
        database    = os.environ.get("SRC_DATABASE")    or "neurinfo",
        charset     = os.environ.get("SRC_CHARSET")     or "utf8")
targetConn = pymysql.connect(
        host        = os.environ.get("TGT_HOST")        or "localhost",
        user        = os.environ.get("TGT_USER")        or "datasets",
        password    = os.environ.get("TGT_PASSWORD")    or "password",
        database    = os.environ.get("TGT_DATABASE")    or "datasets",
        charset     = os.environ.get("TGT_CHARSET")     or "utf8")

sourceCursor = sourceConn.cursor()
targetCursor = targetConn.cursor()

query = "SET FOREIGN_KEY_CHECKS=0"
targetCursor.execute(query)
query = "SET UNIQUE_CHECKS=0"
targetCursor.execute(query)
query = "SET AUTOCOMMIT=0"
targetCursor.execute(query)
query = "SET SQL_LOG_BIN=0"
targetCursor.execute(query)
targetConn.commit()


print("Import related_dataset: start")

sourceCursor.execute("""SELECT STUDY_ID, DATASET_ID FROM REL_STUDY_DATASET""")
bulk_insert(targetCursor, "related_dataset", """
    study_id, dataset_id
    """, sourceCursor)
targetConn.commit()

print("Import related_dataset: end")

query = "SET FOREIGN_KEY_CHECKS=1"
targetCursor.execute(query)
query = "SET UNIQUE_CHECKS=1"
targetCursor.execute(query)
query = "SET AUTOCOMMIT=1"
targetCursor.execute(query)
query = "SET SQL_LOG_BIN=1"
targetCursor.execute(query)
targetConn.commit()


sourceConn.close()
targetConn.close()
