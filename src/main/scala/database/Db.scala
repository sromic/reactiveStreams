package database

import database.drivers.CustomPostgresDiver
import CustomPostgresDiver.api._

/**
 * Created by simun on 26.9.2016..
 */
object Db {
  import config.pgConfig._

  lazy val db = Database.forURL(
    url = s"jdbc:postgresql://$PGHOST:$PGPORT/$PGDATABASE",
    user = "postgres",
    password = "postgres",
    driver = "org.postgresql.Driver"
  )

}
