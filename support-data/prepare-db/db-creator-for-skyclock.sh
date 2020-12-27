#!/bin/sh
db_file="db/skyclock.db"
csv_directory="modified-csv/"
import_csv() {
    sqlite3 -separator , $db_file ".import ${csv_directory}$1 ${table_name}"
}

rm $db_file

# HIP list
table_name="hip_list"
sqlite3 $db_file "CREATE TABLE ${table_name} (hip INTEGER PRIMARY KEY NOT NULL, ra REAL NOT NULL, dec REAL NOT NULL, radius REAL NOT NULL);"
import_csv "hip_lite_major_converted.csv"

# Constellation lines
table_name="constellation_lines"
sqlite3 $db_file "CREATE TABLE ${table_name} (id INTEGER PRIMARY KEY NOT NULL, ra1 REAL NOT NULL, dec1 REAL NOT NULL, ra2 REAL NOT NULL, dec2 REAL NOT NULL);"
import_csv "constellation_lines.csv"

# Milky Way north
table_name="milkyway_north"
sqlite3 $db_file "CREATE TABLE ${table_name} (id INTEGER PRIMARY KEY NOT NULL, x_pos REAL NOT NULL, y_pos REAL NOT NULL, argb INTEGER NOT NULL);"
import_csv "milkyway-pattern-north200.csv"

# Milky Way south
table_name="milkyway_south"
sqlite3 $db_file "CREATE TABLE ${table_name} (id INTEGER PRIMARY KEY NOT NULL, x_pos REAL NOT NULL, y_pos REAL NOT NULL, argb INTEGER NOT NULL);"
import_csv "milkyway-pattern-south200.csv"
