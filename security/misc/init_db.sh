#!/usr/bin/env bash
cat `dirname $0`/legacy-schema.sql | PGPASSWORD=secret psql -U myuser -h localhost mydatabase

# if you want to use the SQL then remember to run:
# update users set password = '{sha256}' || password ;