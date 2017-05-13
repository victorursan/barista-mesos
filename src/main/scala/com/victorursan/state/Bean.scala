package com.victorursan.state

/**
  * Created by victor on 4/23/17.
  */

case class Bean(id: String, name: String, dockerEntity: DockerEntity, pack: Option[String] = None, agentId: Option[String] = None, hostname: Option[String] = None) {
  lazy val taskId: String = pack match {
    case Some(pac) => s"$pac~$name~$id"
    case None => s"$name~$id"
  }
}
case class RawBean(name: String, dockerEntity: DockerEntity, pack: Option[String] = None) {
  def toBean(id: String): Bean = Bean(id, name, dockerEntity, pack)
}