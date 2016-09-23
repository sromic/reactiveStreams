#!/bin/sh

# create the schema
printf "\n-----> Create 'nyc_taxi_data' table...\n\n"
psql -U postgres -f create-schema.sql

# load the data with COPY
printf "\n-----> Loading data...\n\n"
psql -U postgres -f load.sql