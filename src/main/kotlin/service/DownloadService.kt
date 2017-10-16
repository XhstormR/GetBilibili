package service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Path

@Component
open class DownloadService {
    @Autowired
    private lateinit var executorService: ExecutorService

    @Value("#{environment['userAgent']}")
    private lateinit var userAgent: String

    private val args = arrayListOf(
            "aria2c.exe",
            "--input-file=1.txt",
            "--disk-cache=32M",
            "--enable-mmap=true",
            "--max-mmap-limit=2048M",
            "--continue=true",
            "--max-concurrent-downloads=1",
            "--max-connection-per-server=10",
            "--min-split-size=5M",
            "--split=10",
            "--disable-ipv6=true",
            "--http-no-cache=true",
            "--check-certificate=false",
            "--header=Origin: https://www.bilibili.com/",
            "--header=Referer: https://www.bilibili.com/"
    )

    fun start(downloadDir: Path) {
        executorService.execute(args.apply {
            this.add("--dir=$downloadDir")
            this.add("--user-agent=$userAgent")
        })
    }
}
