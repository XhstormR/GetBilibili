import org.springframework.context.annotation.AnnotationConfigApplicationContext
import service.GetBilibili

fun main(args: Array<String>) {
    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog")
    val context = AnnotationConfigApplicationContext(AppConfig::class.java)

    context.getBean(GetBilibili::class.java).run(args)

    context.destroy()
}
