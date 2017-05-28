package com.victorursan.consul

import com.ecwid.consul.v1.catalog.CatalogConsulClient
import com.ecwid.consul.v1.catalog.model.CatalogRegistration

/**
  * Created by victor on 5/10/17.
  */
object Main extends App {

  import java.util
  import java.util.Collections

  import com.ecwid.consul.v1.{ConsulClient, QueryParams}
  import com.ecwid.consul.v1.agent.model.NewService

  val client = new ConsulClient("10.1.1.11", 8500)
  print(ServiceController.serviceHealthChecks(client, "mesos"))
  // set KV
//  val binaryData = Array[Byte](1, 2, 3, 4, 5, 6, 7)
//  client.setKVBinaryValue("someKey", binaryData)
//
//  client.setKVValue("com.my.app.foo", "foo")
//  client.setKVValue("com.my.app.bar", "bar")
//  client.setKVValue("com.your.app.foo", "hello")
//  client.setKVValue("com.your.app.bar", "world")

  // get single KV for key
//  val keyValueResponse = client.getKVValue("com.my.app.foo")
//  System.out.println(keyValueResponse.getValue.getKey + ": " + keyValueResponse.getValue.getDecodedValue) // prints "com.my.app.foo: foo"
//
//
//  // get list of KVs for key prefix (recursive)
//  val keyValuesResponse = client.getKVValues("com.my")
//  keyValuesResponse.getValue.forEach((value) => System.out.println(value.getKey + ": " + value.getDecodedValue)) // prints "com.my.app.foo: foo" and "com.my.app.bar: bar"


  //list known datacenters
//  val response = client.getCatalogDatacenters
//  System.out.println("Datacenters: " + response.getValue)
//  println(client.getAgentServices)
//  client.getAgentMembers.getValue.forEach(member => {
//
//  })
//  val agent = new  ConsulClient("10.32.0.4")
//  println(agent.getAgentServices)
//  client.agentServiceDeregister("hello-world~1")



//  val check = new NewService.Check
//  check.set
//  check.setHttp("http://10.32.0.4:43221")
//  check.setInterval("10s")
//  check.setName("check consul")
//  check.setNode("node1")
//  check.setCheckId("service:consul-agent~1")
//  check.setServiceId("consul-agent~1")
//  check.setStatus(CatalogRegistration.CheckStatus.PASSING)




//  val service = new NewService()
//  service.setName("consul-agent")
////  service.setCheck()
//  service.setAddress("10.32.0.4")
//  service.setPort(8500)
//  service.setId("consul-agent~1")
//
//  agent.agentServiceRegister(service)

//  val service = new NewService()
//  service.setName("hello-world")
//  service.setCheck(check)
//  service.setAddress("10.32.0.4")
//  service.setPort(4321)
//  service.setId("hello-world~2")
////  service.setAddress()
//  agent.agentServiceRegister(service)

//  val catalog = new CatalogConsulClient("localhost")
//  val registration = new CatalogRegistration()
//
//
//
//
//
//  registration.setService(service)
//  registration.setCheck(check)
//  registration.setNode("node1")
//  registration.setAddress("10.32.0.4")
//
//
//
//  catalog.catalogRegister(registration)
//  println(catalog.getCatalogNode("node1", QueryParams.DEFAULT))
//  client.agentServiceDeregister("myapp_02")
//  // register new service
//  val newService = new NewService
//  newService.setId("myapp_01")
//  newService.setName("myapp")
//  newService.setTags(util.Arrays.asList("EU-West", "EU-East"))
//  newService.setPort(8080)
//  client.agentServiceRegister(newService)
//
//  // register new service with associated health check
//  val newService2 = new NewService
//  newService2.setId("myapp_02")
//  newService2.setTags(Collections.singletonList("EU-East"))
//  newService2.setName("myapp")
//  newService2.setPort(8080)
//
//  val serviceCheck = new NewService.Check
//  serviceCheck.setScript("/usr/bin/some-check-script")
//  serviceCheck.setInterval("10s")
//  newService.setCheck(serviceCheck)
//
//  client.agentServiceRegister(newService)
//  client.agentServiceRegister(newService2)

//  client.agentServiceDeregister("myapp_01")
//print(client.agentServiceDeregister("myapp"))
  // query for healthy services based on name (returns myapp_01 and myapp_02 if healthy)
//  val healthyServices = client.getHealthServices("myapp", true, QueryParams.DEFAULT)
//
//  // query for healthy services based on name and tag (returns myapp_01 if healthy)
//  val healthyServices2 = client.getHealthServices("myapp", "EU-West", true, QueryParams.DEFAULT)

}
