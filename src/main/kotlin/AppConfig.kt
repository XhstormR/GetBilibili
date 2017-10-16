import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.PropertySource

@Configuration
@EnableAspectJAutoProxy
@PropertySource("123.properties")
@ComponentScan(basePackages = arrayOf("service"))
open class AppConfig
