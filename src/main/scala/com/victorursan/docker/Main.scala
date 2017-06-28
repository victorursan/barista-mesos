package com.victorursan.docker

import com.victorursan.state.BeanDocker

/**
  * Created by victor on 6/6/17.
  */
object Main extends App {

  val beanDocker = BeanDocker("pack1~hello-world~1", "d9d8504a249c1844f3c83da96481d8024d3d7c0d7eaebd084230ef8d7dab3b0e", "10.1.10.12")
  DockerController
    .registerBeanDocker(beanDocker)
    .subscribe(dockersta => println(dockersta))
  println("12312\n\n\n\n\n\n\n\n\nn\n\n\n\n\n")
}
