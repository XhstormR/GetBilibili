package service

import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.aspectj.lang.annotation.Pointcut
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Aspect
@Component
open class ExecutableChecker {
    private val tempDir: Path by lazy { GetBilibili.tempDir }
    private val links = hashMapOf(
            "7zr.exe" to "http://blog.xhstormr.tk/uploads/bin/7zr.exe",
            "yamdi.exe" to "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhnqa9soj203k03kwfe",
            "aria2c.exe" to "http://ww4.sinaimg.cn/large/a15b4afegw1f7vk9216gvj203k03kb29",
            "ffmpeg.exe" to "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhtyv0wbj203k03k4qz"
    )

    @Autowired
    private lateinit var executorService: ExecutorService

    @Pointcut("execution(* service.ExecutorService.execute(..)) && args(args)")
    private fun executed(args: MutableList<String>) {
    }

    @Before("executed(args)")
    private fun check(args: MutableList<String>) {
        val exe = tempDir.resolve(args[0])
        if (Files.notExists(exe)) {
            getEXE(links[args[0]]!!)
        }
        args[0] = exe.toString()
    }

    private fun getEXE(link: String) {
        if (Files.notExists(tempDir.resolve("7zr.exe"))) {
            getFile(links["7zr.exe"]!!)
        }
        getFile(link)
        executorService.execute(arrayListOf("7zr.exe", "x", "-y", "-bso0", "-bse0", "-bsp2", link.substring(link.lastIndexOf('/') + 1)))
    }

    private fun getFile(link: String) {
        val file = tempDir.resolve(link.substring(link.lastIndexOf('/') + 1)).apply { this.toFile().deleteOnExit() }
        URL(link).openStream().use { Files.copy(it, file, StandardCopyOption.REPLACE_EXISTING) }
    }
}
