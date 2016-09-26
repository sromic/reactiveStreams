package config

/**
 * Created by simun on 26.9.2016..
 */
case class PostgresConfig(
  PGPORT: Int,
  PGUSER: String,
  PGPASSWORD: String,
  PGDATABASE: String,
  PGHOST: String)