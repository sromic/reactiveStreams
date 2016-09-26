package database.drivers

import com.github.tminglei.slickpg._

/**
 * Created by simun on 26.9.2016..
 */
object CustomPostgresDiver extends ExPostgresDriver with PgDate2Support {
  override val api = ExtendedAPI

  object ExtendedAPI extends API with Date2DateTimePlainImplicits
}
