package com.xhstormr.bilibili

import com.xhstormr.bilibili.service.GetBilibili
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.util.logging.LogManager

fun main(args: Array<String>) {
//    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog")
    LogManager.getLogManager().reset()

    val context = AnnotationConfigApplicationContext(AppConfig::class.java)

    context.getBean(GetBilibili::class.java).run(args)

    context.close()
}
