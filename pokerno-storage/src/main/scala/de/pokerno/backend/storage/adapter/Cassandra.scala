package de.pokerno.backend.storage.adapter

import de.pokerno.backend.storage.{PlayHistory, Store}

import com.datastax.driver.core.{Cluster, SocketOptions, PoolingOptions}

object Cassandra {
   
  class Client(
      points: String
  ) extends Store.Client {
    
    private val pools = new PoolingOptions
    private val cluster = new Cluster.Builder()
                                      .addContactPoints(points)
                                      .withPoolingOptions(pools)
                                      .withSocketOptions(new SocketOptions().setTcpNoDelay(true))
                                      .build()
    
    private val client = cluster.connect
                                      
    def write(e: PlayHistory.Entry) {
      // TODO
    }
  }
  
}
