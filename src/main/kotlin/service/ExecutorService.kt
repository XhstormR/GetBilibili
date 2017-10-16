package service

import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
open class ExecutorService {
    private companion object {
        private val tasks = HashSet<Process>()
        private val tempDir: Path by lazy { GetBilibili.tempDir }

        init {
            Runtime.getRuntime().addShutdownHook(Thread { tasks.forEach { it.destroyForcibly() } })
        }
    }

    open fun execute(args: MutableList<String>) {//注意：方法需加上 open 修饰符才能够被横切
        val process = ProcessBuilder(args)
                .directory(tempDir.toFile())
                .redirectErrorStream(true)
                .redirectInput(ProcessBuilder.Redirect.INHERIT)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start()
        tasks.add(process)
        process.waitFor()
        tasks.remove(process)
    }
}
