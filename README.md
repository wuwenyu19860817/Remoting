# Remoting
----------------------------------------------简介-------------------------------------------------------------
基于插件化的服务化框架Remoting
Remoting是一个 '多服务' '多版本' 微内核插件化的服务化框架.

多服务：一个插件对外只发布一个服务。
多版本：一个服务有多个版本，是真正意义上的多版本(通过jvm类加载器进行多版本隔离)。
微内核：Remoting本身。
插件化：扩展插件可以通过Remoting发布的规范进行开发。 

定位:
定位于一些专用的服务化场景，解决升级维护问题，传统服务化框架会考虑服务与服务之间的依赖关系，而这里的插件化机制，每个插件都是一个完善的整体，不考虑插件之间的依赖关系，如果插件之间一定要发生依赖通过微内核模块发生简单的关系，不同于大众化全面化的服务化框架dubbo。

----------------------------------------------技术选型-------------------------------------------------------------
高性能的通讯:基于当前开源的成熟稳定的通讯框架netty。
插件化安装部署:基于java的类加载机制进行开发(OSGi上手慢学习成本太高，过多考虑插件之间的依赖关系，暂不考虑)。

----------------------------------------------模块划分-------------------------------------------------------------
内核模块 
远程通讯模块+插件化模块

扩展插件
扩展插件又分为系统插件和业务插件 
系统插件主要负责对业务插件以及Remoting内核 进行监控治理。




/**
 * Remoting通信协议 v1.0.0
 *
 * 协议格式 <length> <header length> <header data> <body data> 1 2 3 4 协议分4部分，
 * 含义分别如下
     1、大端4个字节整数，等于2、3、4长度总和
     2、大端4个字节整数，等于3的长度
     3、使用json序列化数据
     4、应用自定义二进制序列化数据
 */
 


                                                                              Remoting插件定义规范 v1.0.0
一个插件对外一个服务
一个服务对外多个版本

--------------------------------------------------------------------------------插件目录结构规范 ---------------------------------------------------------------------------------------------------------------                        

目录结构                                                                                                                                  描述                                                                                                                             校验规则
rootDir                                                                ---------插件根目录                                                                                                                   目录名不限  目录必须存在
             1-1.0.0                                                   ---------插件目录命名规则"sid-version"                                             目录名中间带"-" 目录可以不存在
                        lib                                            ---------插件目录下lib目录存放依赖的所有jar                                         目录名为"lib"目录必须存在
                              a1.jar                                   
                              b1.jar
                              c1.jar
                              d1.jar
                        resources                                      ---------插件资源目录 存放所有的资源类加载器的classpath                              目录名为"resources"目录必须存在  
                              1.xml
                              2.properties
                        menifest.properties                            ---------配置描述信息,供程序读取 详见menifest.properties描述文件规范                               文件名为"menifest.properties" 文件必须存在  
                         
                         
                      
                      
                      
--------------------------------------------------------------------------------menifest.properties描述文件规范---------------------------------------------------------------------------------------------------------------                        
                        
属性名                                                                                                                                      描述                                                                                                                             校验规则                                                                             
Entrypoint=org.salmon.remoting.test.MyProcess                          ---------插件服务入口类                                                                                                            不能为空,必须实现接口实现接口org.salmon.remoting.netty.NettyRequestProcessor      
Sid=1                                                                  ---------具体服务编号                                                                                                               不能为空,同插件目录命名规则"sid-version"
Version=1.0.0                                                          ---------具体版本名称                                                                                                               不能为空,同插件目录命名规则"sid-version"
Threadpool.enable=true                                                 ---------是否启用线程池                                                                                                            必须布尔类型
Threadpool.corePoolSize=50                                             ---------启用线程池后生效                                                                                                        大于0整数
Threadpool.maximumPoolSize=50                                          ---------启用线程池后生效                                                                                                        大于0整数
Threadpool.keepAliveTimeInSecond=60                                    ---------启用线程池后生效                                                                                                        大于0整数
Threadpool.queqeSize=50                                                ---------启用线程池后生效                                                                                                        大于0整数


注:插件规范稳定后通过maven插件来规范打包(类似war插件)
