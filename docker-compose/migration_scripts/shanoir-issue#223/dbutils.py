#!/usr/bin/env python
# -*- coding: utf-8 -*-

import itertools

def take(it, n):
    """take the next n elements of an iterator and return them as a list"""
    result = []
    try:
        for i in range(n):
            result.append(next(it))
    except StopIteration:
            pass
    return result

def bulk_insert(cursor, table, columns, values, chunksize=1000):
    """db bulk insertions
    
    This execute an insert request with multiple rows:
        INSERT INTO {table} ({columns}) VALUES ({value[0]}), (values[1]), ... (values[n]);

    cursor (db cursor): target db cursor
    table (str): target table name
    columns (str): comma-separated list of column names
    values (db cursor or iterator): input values (yields a sequence of column values to be inserted)
    chunksize (int): number of rows per request
    """
    nb_col = len(columns.split(","))
    row_tpl = "(%s)" % ", ".join(itertools.repeat("%s", nb_col))
    if hasattr(values, "fetchmany"):
        # db cursor
        next_chunk = lambda: values.fetchmany(chunksize)
    else:
        # any iterable
        it = iter(values)
        next_chunk = lambda: take(it, chunksize)
    while True:
        rows = next_chunk()
        if not rows:
            return
        cursor.execute(
                "INSERT INTO %s (%s) VALUES %s" % (table, columns,
                    ", ".join(itertools.repeat(row_tpl, len(rows)))),
                list(itertools.chain(*rows)))
