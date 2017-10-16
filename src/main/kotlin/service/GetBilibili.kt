package service

import com.google.gson.JsonParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.File
import java.io.StringReader
import java.lang.Character.UnicodeBlock.KATAKANA
import java.lang.Character.UnicodeBlock.LATIN_1_SUPPLEMENT
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.text.DecimalFormat
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory

@Component
open class GetBilibili {
    private lateinit var commandLine: CommandLine
    private val cookie: String by lazy { if (commandLine.hasOption("cookie")) baseDir.resolve("cookie.txt").toFile().readText() else "" }
    private val isDelete: Boolean by lazy { commandLine.hasOption("delete") }
    private val isConvert: Boolean by lazy { commandLine.hasOption("convert") }
    private val downloadDir: Path by lazy { if (commandLine.getOptionValue("downloadDir") != null) Paths.get(commandLine.getOptionValue("downloadDir"), "GetBilibili") else baseDir.resolve("GetBilibili") }
    private val videoLink = arrayListOf<String>()
    private var videoSize = 0L
    private var videoLength = 0
    private var videoTitle: String? = null
    private val options = Options().apply {
        this.addOption("l", true, "Get bilibili ultra-definition video link")
        this.addOption("d", true, "Download bilibili ultra-definition video")
        this.addOption("m", false, "Merge segmented video")
        this.addOption("h", false, "Print a help message")
        this.addOption("cookie", false, "(Default: false) Specify the cookie in cookie.txt")
        this.addOption("delete", false, "(Default: false) Delete segmented video after completion")
        this.addOption("convert", false, "(Default: false) Convert FLV to MP4 after completion")
        this.addOption("downloadDir", true, "(Default: Jar Dir) Specify the mergeService/merge directory")
    }
    @Autowired
    private lateinit var downloadService: DownloadService
    @Autowired
    private lateinit var mergeService: MergeService

    companion object {
        val tempDir: Path = Paths.get(System.getenv("APPDATA"), "GetBilibili")
        val baseDir: Path = URLDecoder.decode(GetBilibili::class.java.protectionDomain.codeSource.location.path, "utf-8")!!.let { Paths.get(it.substring(1, it.lastIndexOf('/') + 1)) }
    }

    fun run(args: Array<String>) {
        commandLine = DefaultParser().parse(options, args)

        if (commandLine.getOptionValue('l') != null) {
            generateLink(commandLine.getOptionValue('l'))
            showLink()
            println("\nDone!")
            return
        }
        if (commandLine.getOptionValue('d') != null) {
            generateLink(commandLine.getOptionValue('d'))
            createDirectory()
            saveLink()
            downLoad()
            listFile()
            mergeFLV()
            println("\nDone!")
            return
        }
        if (commandLine.hasOption('m')) {
            createDirectory()
            listFile()
            mergeFLV()
            println("\nDone!")
            return
        }
        if (commandLine.hasOption('h')) {
            printHelp()
            return
        }
        printHelp()
    }

    private fun printHelp() {
        val help = HelpFormatter()
        help.optionComparator = null
        help.printHelp(150, "GetBilibili.jar", "", options, "", true)
    }

    private fun generateLink(url: String) {
        if (url.contains("playurl") && url.contains("json")) {
            parseJSON(url)
        } else if (url.contains("playurl")) {
            parseXML(url)
        }
    }

    private fun createDirectory() {
        if (Files.notExists(tempDir)) {
            Files.createDirectories(tempDir)
        }
        if (Files.notExists(downloadDir)) {
            Files.createDirectories(downloadDir)
        }
    }

    private fun showLink() {
        val sizeFormat = DecimalFormat("0.00")
        val numFormat = DecimalFormat("0,000")
        val timeFormat = DecimalFormat("00")
        val s = videoLength / 1000//秒
        val m = s / 60//分
        val h = m / 60//时
        println("Title: ${getFileName() + if (isConvert) ".mp4" else ".flv"}")
        System.out.printf("Total Size: %s MB (%s bytes)\tTotal Time: %s:%s:%s (%s:%s Mins)\n\n", sizeFormat.format(videoSize / (1024 * 1024.0)), numFormat.format(videoSize), timeFormat.format(h.toLong()), timeFormat.format((m % 60).toLong()), timeFormat.format((s % 60).toLong()), timeFormat.format(m.toLong()), timeFormat.format((s % 60).toLong()))
        videoLink.forEach(::println)
    }

    private fun saveLink() {
        val link = tempDir.resolve("1.txt")
        link.toFile().deleteOnExit()
        Files.write(link, videoLink, Charset.forName("utf-8"))
    }

    private fun downLoad() {
        downloadService.start(downloadDir)
    }

    private fun listFile() {
        val fileList = tempDir.resolve("2.txt")
        fileList.toFile().deleteOnExit()

        val paths: List<File> = downloadDir.toFile().listFiles { _, name -> name.endsWith(".flv") || name.endsWith(".mp4") }.sortedBy {
            val name1 = it.name
            val s1 = name1.substring(name1.indexOf("-") + 1, name1.indexOf("."))
            val pattern = Pattern.compile("\\d+")
            val matcher1 = pattern.matcher(s1)
            if (matcher1.find()) Integer.valueOf(matcher1.group()) else 0
        }

        val s = "file 'X'"
        Files.newBufferedWriter(fileList, charset("utf-8")).use { paths.forEach { o -> it.write(s.replace("X", o.toString()) + "\n") } }
    }

    private fun mergeFLV() {
        val tempFile = downloadDir.resolveSibling("123.flv")
        val finalFile = downloadDir.resolveSibling(getFileName() + if (isConvert) ".mp4" else ".flv")

        if (videoLink.size == 1) {
            val s = videoLink[0]
            val i = s.indexOf('?')
            val name = s.substring(s.lastIndexOf('/', i) + 1, i)
            Files.move(downloadDir.resolve(name), downloadDir.resolveSibling(getFileName() + name.substring(name.lastIndexOf('.'))), REPLACE_EXISTING)//移动文件至上层目录
        } else {
            mergeService.start(tempFile, finalFile, isConvert)
        }

        Files.deleteIfExists(tempFile)
        if (isDelete) {
            downloadDir.toFile().deleteRecursively()
        }
    }

    private fun getFileName(): String {
        if (videoTitle == null) {
            return "Video"
        }
        val stringBuilder = StringBuilder()
        for (c in videoTitle!!.toCharArray()) {
            val unicodeBlock = Character.UnicodeBlock.of(c)
            stringBuilder.append(if (unicodeBlock == LATIN_1_SUPPLEMENT || unicodeBlock == KATAKANA) "" else c)
        }
        val s = stringBuilder.toString()
        return s.replace('/', ' ').replace('\\', ' ').replace(':', ' ').replace('*', ' ').replace('?', ' ').replace('"', ' ').replace('<', ' ').replace('>', ' ').replace('|', ' ').replace('‧', ' ').replace('•', ' ')
    }

    private fun parseJSON(link: String) {
        try {
            URL(link).openConnection().apply { this.setRequestProperty("Cookie", cookie) }.inputStream.bufferedReader().use {
                val jsonObject = JsonParser().parse(it).asJsonObject
                val jsonArray = jsonObject.getAsJsonArray("durl")
                jsonArray.forEach {
                    val durlObject = it.asJsonObject
                    videoLink.add(durlObject.get("url").asString)
                    videoSize += durlObject.get("size").asInt.toLong()
                    videoLength += durlObject.get("length").asInt
                }
            }
        } catch (e: NullPointerException) {
            throw IllegalStateException("KEY 已经失效啦！好难找的伐！")
        }
    }

    private fun parseXML(link: String) {
        val xml: String = URL(link).openConnection().apply { this.setRequestProperty("Cookie", cookie) }.inputStream.bufferedReader().useLines {
            val stringBuilder = StringBuilder()
            it.forEach { stringBuilder.append(it) }
            stringBuilder.toString()/*XML 文件*/
        }
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(InputSource(StringReader(xml)))/*XML 对象*/

        val durl = document.getElementsByTagName("durl")
        for (i in 0..durl.length - 1) {
            val element = durl.item(i) as Element
            videoLink.add(element.getElementsByTagName("url").item(0).firstChild.nodeValue)
            videoSize += java.lang.Long.valueOf(element.getElementsByTagName("size").item(0).firstChild.nodeValue)!!
            videoLength += Integer.valueOf(element.getElementsByTagName("length").item(0).firstChild.nodeValue)!!
        }
    }
}
