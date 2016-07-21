#!/bin/bash
mkdir -p backup
NOW=$(date +"%Y_%m_%d_%H_%M_%S")
mysqldump -u pleak -pSV9bhSburbBnh5jTJVZ5UT5e pleak > backup/db.sql
tar -zcf backup/$NOW.tar.gz backup/db.sql src/main/webapp/files
rm backup/db.sql
