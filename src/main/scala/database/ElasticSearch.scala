package database

import com.sksamuel.elastic4s.{ElasticsearchClientUri, ElasticClient}
import org.elasticsearch.common.settings.Settings

/**
 * Created by simun on 26.9.2016..
 */
object ElasticSearch {

  import config.esConfig._

  val esClient = {

    val settings = Settings
      .settingsBuilder()
      .put("cluster.name", ESCLUSTERNAME)
      .build()

    ElasticClient.transport(settings, ElasticsearchClientUri(ESHOST, ESPORT.toInt))
  }
}
