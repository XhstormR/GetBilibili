package service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
open class MergeService {
    @Autowired
    private lateinit var executorService: ExecutorService

    private val args1 = arrayListOf(
            "ffmpeg.exe",
            "-f",
            "concat",
            "-safe",
            "-1",
            "-i",
            "2.txt",
            "-c",
            "copy",
            "-y"
    )
    private val args2 = arrayListOf(
            "ffmpeg.exe",
            "-i",
            "|2|",
            "-c",
            "copy",
            "-y"
    )
    private val args3 = arrayListOf(
            "yamdi.exe",
            "-i",
            "|2|",
            "-o"
    )

    fun start(tempFile: Path, finalFile: Path, convert: Boolean) {
        println("\nMerging...")
        executorService.execute(args1.apply {
            this.add(tempFile.toString())
        })

        if (convert) {
            println("\nConverting...")
            executorService.execute(args2.apply {
                this[2] = tempFile.toString()
                this.add(finalFile.toString())
            })
        } else {
            println("\nMerging...")
            executorService.execute(args3.apply {
                this[2] = tempFile.toString()
                this.add(finalFile.toString())
            })
        }
    }
}
