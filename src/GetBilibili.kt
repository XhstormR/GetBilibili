import com.google.gson.JsonParser
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.StringReader
import java.lang.Character.UnicodeBlock.KATAKANA
import java.lang.Character.UnicodeBlock.LATIN_1_SUPPLEMENT
import java.lang.ProcessBuilder.Redirect
import java.net.URL
import java.net.URLDecoder
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.security.MessageDigest
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException
import javax.xml.parsers.DocumentBuilderFactory

object GetBilibili {
    private val UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36"
    private val Aria2Link = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vk9216gvj203k03kb29"
    private val YamdiLink = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhnqa9soj203k03kwfe"
    private val FFmpegLink = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhtyv0wbj203k03k4qz"
    private val SevenZipLink = "http://blog.xhstormr.tk/uploads/bin/7zr.exe"
    private val Appkey = "NmY5MGE1OWFjNThhNDEyMw=="
    private val Secretkey = "MGJmZDg0Y2MzOTQwMDM1MTczZjM1ZTY3Nzc1MDgzMjY="
    private val dir: Path by lazy { if (parse.getOptionValue("dir") != null) Paths.get(parse.getOptionValue("dir"), "GetBilibili") else URLDecoder.decode(GetBilibili::class.java.protectionDomain.codeSource.location.path, "utf-8")!!.let { Paths.get(it.substring(1, it.lastIndexOf('/') + 1), "GetBilibili") } }
    private val cookie: String by lazy { parse.getOptionValue("cookie") ?: "" }//DedeUserID=1426753; DedeUserID__ckMd5=427ebfe30d4f15eb; SESSDATA=f204dbc8%2C1E98438047%2Cfe76287b; sid=9y6y864j
    private val isDelete: Boolean by lazy { parse.hasOption("delete") }
    private val isConvert: Boolean by lazy { parse.hasOption("convert") }
    private val parse: CommandLine  by lazy { DefaultParser().parse(options, args) }
    private val videoLink = ArrayList<String>()
    private val tasks = HashSet<Process>()
    private val tempDir = Paths.get(System.getenv("APPDATA"), "GetBilibili")
    private val options = Options()
    private var videoSize = 0L
    private var videoLength = 0
    private var videoCid: String? = null
    private var videoTitle: String? = null
    private var args: Array<String>? = null

    init {
        Runtime.getRuntime().addShutdownHook(Thread { tasks.forEach { it.destroyForcibly() } })
        options.addOption("l", true, "Get bilibili ultra-definition video link")
        options.addOption("d", true, "Download bilibili ultra-definition video")
        options.addOption("m", false, "Merge segmented video")
        options.addOption("h", false, "Print a help message")
        options.addOption("delete", false, "(Default: false) Delete segmented video after completion")
        options.addOption("convert", false, "(Default: false) Convert FLV to MP4 after completion")
        options.addOption("dir", true, "(Default: Jar Dir) Specify the download/merge directory")
        options.addOption("cookie", true, "(Default: null) Specify the cookie")
    }

    @JvmStatic fun main(args: Array<String>) {
        this.args = args

        println()
        if (parse.getOptionValue('l') != null) {
            generateLink(parse.getOptionValue('l'))
            showLink()
            println("\nDone!")
            return
        }
        if (parse.getOptionValue('d') != null) {
            generateLink(parse.getOptionValue('d'))
            createDirectory()
            saveLink()
            downLoad()
            listFile()
            mergeFLV()
            println("\nDone!")
            return
        }
        if (parse.hasOption('m')) {
            createDirectory()
            listFile()
            mergeFLV()
            println("\nDone!")
            return
        }
        if (parse.hasOption('h')) {
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
        } else {
            getCID(url)
            getLink()
        }
    }

    private fun createDirectory() {
        if (Files.notExists(tempDir)) {
            Files.createDirectories(tempDir)
        }
        if (Files.notExists(dir)) {
            Files.createDirectories(dir)
        }
    }

    private fun getCID(url: String) {
        val connection = URL(url).openConnection()
        connection.setRequestProperty("Cookie", cookie)

        val bufferedReader = try {
            BufferedReader(InputStreamReader(GZIPInputStream(connection.inputStream), "utf-8"))
        } catch (e: ZipException) {
            BufferedReader(InputStreamReader(connection.inputStream, "utf-8"))
        }

        var x = false
        var y = false
        bufferedReader.useLines {
            it.forEach {
                if (it.contains("<div class=\"msgbox\"><div class=\"content\"><a id='login-btn'>登录</a></div></div>")) {
                    throw IllegalArgumentException("此为隐藏视频，需要设置 Cookie。")
                }
                if (!x || !y) {
                    when {
                        url.contains("video") -> {
                            if (it.contains("cid=")) {
                                x = true
                                videoCid = it.substring(it.indexOf("cid=") + 4, it.indexOf('&'))
                            }
                            if (it.contains("<h1 title=")) {
                                y = true
                                val i = it.lastIndexOf("</h1>")
                                videoTitle = it.substring(it.lastIndexOf('>', i) + 1, i)
                            }
                        }
                        url.contains("movie") -> {
                            if (it.contains("cid=")) {
                                x = true
                                videoCid = it.substring(it.indexOf("cid=") + 5, it.indexOf("cid=") + 13)
                            }
                            if (it.contains("pay_top_msg")) {
                                y = true
                                val i = it.lastIndexOf("</div>")
                                videoTitle = it.substring(it.lastIndexOf('>', i) + 1, i)
                            }
                        }
                        url.contains("anime") -> {
                            if (it.contains("v-av-link")) {
                                x = true
                                val i = it.lastIndexOf("</a>")
                                val aid = it.substring(it.lastIndexOf('>', i) + 3, i)
                                BufferedReader(InputStreamReader(GZIPInputStream(URL("http://www.bilibili.com/widget/getPageList?aid=" + aid).openStream()), "utf-8")).use { videoCid = JsonParser().parse(it).asJsonArray.get(0).asJsonObject.get("cid").asString }
                            }
                            if (it.contains("<h1 title=")) {
                                y = true
                                val i = it.lastIndexOf("</h1>")
                                videoTitle = it.substring(it.lastIndexOf('>', i) + 1, i)
                            }
                        }
                        else -> throw IllegalStateException("此链接需要更新，请告知开发者！")
                    }
                }
            }
            if (!x) throw IllegalStateException("此链接需要更新，请告知开发者！")
        }
    }

    private fun getLink() {
        val SubLink = StringBuilder().append("appkey=").append(String(Base64.getDecoder().decode(Appkey))).append("&cid=").append(videoCid).append("&otype=json&quality=3&type=flv")
        val Sign = hash(SubLink.toString() + String(Base64.getDecoder().decode(Secretkey)), "MD5")
        parseJSON("http://interface.bilibili.com/playurl?$SubLink&sign=$Sign")
    }

    private fun showLink() {
        val sizeFormat = DecimalFormat("0.00")
        val numFormat = DecimalFormat("0,000")
        val timeFormat = DecimalFormat("00")
        val s = videoLength / 1000//秒
        val m = s / 60//分
        val h = m / 60//时
        println("Title: ${fileName + if (isConvert) ".mp4" else ".flv"}")
        System.out.printf("Total Size: %s MB (%s bytes)\tTotal Time: %s:%s:%s (%s:%s Mins)\n\n", sizeFormat.format(videoSize / (1024 * 1024.0)), numFormat.format(videoSize), timeFormat.format(h.toLong()), timeFormat.format((m % 60).toLong()), timeFormat.format((s % 60).toLong()), timeFormat.format(m.toLong()), timeFormat.format((s % 60).toLong()))
        videoLink.forEach(::println)
    }

    private fun saveLink() {
        val link = tempDir.resolve("1.txt")
        link.toFile().deleteOnExit()
        Files.write(link, videoLink, Charset.forName("utf-8"))
    }

    private fun downLoad() {
        if (Files.notExists(tempDir.resolve("aria2c.exe"))) {
            getEXE(Aria2Link)
        }
        execute(tempDir.resolve("aria2c.exe").toString(), "--input-file=1.txt", "--dir=" + dir.toString(), "--disk-cache=32M", "--user-agent=" + UserAgent, "--enable-mmap=true", "--max-mmap-limit=2048M", "--continue=true", "--max-concurrent-downloads=1", "--max-connection-per-server=10", "--min-split-size=5M", "--split=10", "--disable-ipv6=true", "--http-no-cache=true", "--check-certificate=false")
    }

    private fun listFile() {
        val fileList = tempDir.resolve("2.txt")
        fileList.toFile().deleteOnExit()

        val paths: List<File> = dir.toFile().listFiles { dir, name -> name.endsWith(".flv") || name.endsWith(".mp4") }.sortedBy {
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
        val tempFile = dir.resolveSibling("123.flv")
        val finalFile = dir.resolveSibling(fileName + if (isConvert) ".mp4" else ".flv")

        if (videoLink.size == 1) {
            val s = videoLink[0]
            val i = s.indexOf('?')
            val name = s.substring(s.lastIndexOf('/', i) + 1, i)
            Files.move(dir.resolve(name), dir.resolveSibling(fileName + name.substring(name.lastIndexOf('.'))), REPLACE_EXISTING)//移动文件至上层目录
        } else {
            println("\nMerging...")
            if (Files.notExists(tempDir.resolve("ffmpeg.exe"))) {
                getEXE(FFmpegLink)
            }
            execute(tempDir.resolve("ffmpeg.exe").toString(), "-f", "concat", "-safe", "-1", "-i", "2.txt", "-c", "copy", "-y", tempFile.toString())

            if (isConvert) {
                println("\nConverting...")
                execute(tempDir.resolve("ffmpeg.exe").toString(), "-i", tempFile.toString(), "-c", "copy", "-y", finalFile.toString())
            } else {
                println("\nMerging...")
                if (Files.notExists(tempDir.resolve("yamdi.exe"))) {
                    getEXE(YamdiLink)
                }
                execute(tempDir.resolve("yamdi.exe").toString(), "-i", tempFile.toString(), "-o", finalFile.toString())
            }
        }

        Files.deleteIfExists(tempFile)
        if (isDelete) {
            dir.toFile().deleteRecursively()
        }
    }

    private val fileName: String
        get() {
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

    private fun getEXE(link: String) {
        if (Files.notExists(tempDir.resolve("7zr.exe"))) {
            getFile(SevenZipLink)
        }
        getFile(link)
        execute(tempDir.resolve("7zr.exe").toString(), "x", "-y", link.substring(link.lastIndexOf('/') + 1))
    }

    private fun getFile(link: String) {
        val connection = URL(link).openConnection()
        connection.setRequestProperty("User-Agent", UserAgent)
        val path = tempDir.resolve(link.substring(link.lastIndexOf('/') + 1))
        path.toFile().deleteOnExit()

        connection.inputStream.use { Files.copy(it, path, REPLACE_EXISTING) }
    }

    private fun execute(vararg command: String) {
        val process = ProcessBuilder(*command).directory(tempDir.toFile()).redirectErrorStream(true).redirectOutput(Redirect.INHERIT).redirectInput(Redirect.INHERIT).start()
        tasks.add(process)
        process.waitFor()
        tasks.remove(process)
    }

    private fun hash(str: String, algorithm: String): String {
        val instance = MessageDigest.getInstance(algorithm)
        val digest = instance.digest(str.toByteArray())

        var stringBuilder = StringBuilder()
        digest
                .asSequence()
                .map { Integer.toHexString(it.toInt() and 0xff) }
                .forEach { stringBuilder = if (it.length < 2) stringBuilder.append('0').append(it) else stringBuilder.append(it) }
        return stringBuilder.toString()
    }

    private fun parseJSON(link: String) {
        val connection = URL(link).openConnection()
        connection.setRequestProperty("Cookie", cookie)

        try {
            connection.inputStream.bufferedReader().use {
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
        val connection = URL(link).openConnection()
        connection.setRequestProperty("Cookie", cookie)
        val xml: String = connection.inputStream.bufferedReader().useLines {
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
