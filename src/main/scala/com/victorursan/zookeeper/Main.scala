package com.victorursan.zookeeper

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.victorursan.state.{Bean, DockerEntity, DockerResource}
import com.victorursan.utils.JsonSupport
import spray.json._

import scala.util.Try

 /**
  * Created by victor on 4/23/17.
  */
object Main extends App with JsonSupport{
   val payload = "payload".getBytes
   CuratorService.createOrUpdate("/a/data", payload)

   val received = CuratorService.read("/a/data")

   print(payload.equals(received))
   CuratorService.delete("/a")

//  val x =  DockerEntity("a", "b", resource = DockerResource(1, 2))
//  val y = List(Bean(x), Bean(x.copy(name="ghiut")))
//  CuratorService.delete("/barista/state/get")
//  print(y)
//    CuratorService.create("/barista/state/get", y.toJson.toString().getBytes())
//
//    val g = new String(CuratorService.read("/barista/state/get")).parseJson.convertTo[List[Bean]]
//  print(g)
////  CuratorService.create("/crud/adaasdas", "a is a node".getBytes)
////  CuratorService.delete("/crud")
////  val z = y.toJson
////  val q = z.toString().parseJson.convertTo[List[Bean]]
////  val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
////  val oos = new ObjectOutputStream(stream)
////  oos.writeObject(y)
////  oos.close
////  val xyz = stream.toByteArray
////
////  val ois = new ObjectInputStream(new ByteArrayInputStream(xyz))
////  val value = ois.readObject
////  ois.close
////  val zzz = value

}