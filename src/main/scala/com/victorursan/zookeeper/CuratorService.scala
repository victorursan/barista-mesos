package com.victorursan.zookeeper

import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

/**
  * Created by victor on 4/23/17.
  */
object CuratorService {
  private val zkConnection: String = "127.0.0.1:2181"
  private val client: CuratorFramework = CuratorFrameworkFactory.newClient(zkConnection, new ExponentialBackoffRetry(1000, 3))
  client.start()
  client.blockUntilConnected()

  def createOrUpdate(path: String, payload: Array[Byte], compressed: Boolean = false): Unit =
    if (client.checkExists().forPath(path) != null) update(path, payload, compressed) else create(path, payload, compressed)


  def create(path: String, payload: Array[Byte], compressed: Boolean = false): Unit =
    if (compressed) {
      client.create()
        .compressed()
        .creatingParentsIfNeeded()
        .forPath(path, payload)
    } else {
      client.create()
        .creatingParentsIfNeeded()
        .forPath(path, payload)
    }

  def update(path: String, payload: Array[Byte], compressed: Boolean = false): Unit =
    if (compressed) {
      client.setData()
        .compressed()
        .forPath(path, payload)
    } else {
      client.setData()
        .forPath(path, payload)
    }

  def read(path: String, decompressed: Boolean = false): Array[Byte] =
    if (decompressed) {
      client.getData
        .decompressed()
        .forPath(path)
    } else {
      client.getData
        .forPath(path)
    }


  def delete(path: String): Unit =
    client.delete()
      .deletingChildrenIfNeeded()
      .forPath(path)

}
