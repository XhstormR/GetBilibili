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
            "-c",
            "copy",
            "-y",
            "-i",
            "2.txt"
    )
    private val args2 = arrayListOf(
            "ffmpeg.exe",
            "-c",
            "copy",
            "-y",
            "-i"
    )
    private val args3 = arrayListOf(
            "yamdi.exe",
            "-i",
            "|2|",
            "-o",
            "|4|"
    )

    fun start(tempFile: Path, finalFile: Path, convert: Boolean) {
        println("\nMerging...")
        executorService.execute(args1.apply {
            this.add(tempFile.toString())
        })

        if (convert) {
            println("\nConverting...")
            executorService.execute(args2.apply {
                this.add(tempFile.toString())
                this.add(finalFile.toString())
            })
        } else {
            println("\nMerging...")
            executorService.execute(args3.apply {
                this[2] = tempFile.toString()
                this[4] = finalFile.toString()
            })
        }
    }
}
