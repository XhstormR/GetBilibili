package com.xhstormr.bilibili

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan
@EnableAspectJAutoProxy
@PropertySource("classpath:/config.properties")
open class AppConfig
