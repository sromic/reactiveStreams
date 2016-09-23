DROP TABLE nyc_taxi_data;

CREATE TABLE nyc_taxi_data (
  vendor_id integer NOT NULL,
  tpep_pickup_datetime timestamp WITHOUT TIME ZONE NOT NULL,
  tpep_dropoff_datetime timestamp WITHOUT TIME ZONE NOT NULL,
  passenger_count integer NOT NULL,
  trip_distance numeric NOT NULL,
  pickup_longitude numeric NOT NULL,
  pickup_latitude numeric NOT NULL,
  rate_code_id integer NOT NULL,
  store_and_fwd_flag boolean NOT NULL,
  dropoff_longitude numeric NOT NULL,
  dropoff_latitude numeric NOT NULL,
  payment_type integer NOT NULL,
  fare_amount numeric NOT NULL,
  extra numeric NOT NULL,
  mta_tax numeric NOT NULL,
  tip_amount numeric NOT NULL,
  tolls_amount numeric NOT NULL,
  improvement_surcharge numeric NOT NULL,
  total_amount numeric NOT NULL
);