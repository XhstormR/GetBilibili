import com.google.gson.JsonParser
import org.apache.commons.cli.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.*
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
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

object GetBilibili {
    private val UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36"
    private val Aria2Link = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vk9216gvj203k03kb29"
    private val YamdiLink = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhnqa9soj203k03kwfe"
    private val FFmpegLink = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhtyv0wbj203k03k4qz"
    private val SevenZipLink = "http://blog.xhstormr.tk/uploads/bin/7zr.exe"
    private val Appkey = "NmY5MGE1OWFjNThhNDEyMw=="
    private val Secretkey = "MGJmZDg0Y2MzOTQwMDM1MTczZjM1ZTY3Nzc1MDgzMjY="
    private var Dir: Path? = null
    private var TempDir: Path? = null
    private var Cookie: String? = null//DedeUserID=1426753; DedeUserID__ckMd5=427ebfe30d4f15eb; SESSDATA=f204dbc8%2C1E98438047%2Cfe76287b; sid=9y6y864j
    private var Video_Cid: String? = null
    private var Video_Title: String? = null
    private var Video_Size: Long = 0
    private var Video_Length: Int = 0
    private var isDelete: Boolean = false
    private var isConvert: Boolean = false
    private var Link: List<String>? = null
    private val Tasks = HashSet<Process>()

    init {
        Runtime.getRuntime().addShutdownHook(Thread { Tasks.forEach { it.destroyForcibly() } })
    }

    @Throws(IOException::class, InterruptedException::class, NoSuchAlgorithmException::class, ParserConfigurationException::class, SAXException::class)
    @JvmStatic fun main(args: Array<String>) {
        println()
        val options = Options()
        options.addOption("l", true, "Get bilibili ultra-definition video link")
        options.addOption("d", true, "Download bilibili ultra-definition video")
        options.addOption("m", false, "Merge segmented video")
        options.addOption("h", false, "Print a help message")
        options.addOption("delete", false, "(Default: false) Delete segmented video after completion")
        options.addOption("convert", false, "(Default: false) Convert FLV to MP4 after completion")
        options.addOption("dir", true, "(Default: Jar Dir) Specify the download/merge directory")
        options.addOption("cookie", true, "(Default: null) Specify the cookie")

        val parse: CommandLine
        try {
            parse = DefaultParser().parse(options, args)
        } catch (e: ParseException) {
            println(e.message + "\n")
            printHelp(options)
            return
        }

        isDelete = parse.hasOption("delete")
        isConvert = parse.hasOption("convert")
        Cookie = if (parse.getOptionValue("cookie") != null) parse.getOptionValue("cookie") else ""

        if (parse.getOptionValue('l') != null) {
            generateLink(parse.getOptionValue('l'))
            showLink()
            println("\nDone!")
            return
        }
        if (parse.getOptionValue('d') != null) {
            generateLink(parse.getOptionValue('d'))
            createDirectory(parse)
            saveLink()
            downLoad()
            listFile()
            mergeFLV()
            println("\nDone!")
            return
        }
        if (parse.hasOption('m')) {
            createDirectory(parse)
            listFile()
            mergeFLV()
            println("\nDone!")
            return
        }
        if (parse.hasOption('h')) {
            printHelp(options)
            return
        }
        printHelp(options)
    }

    private fun printHelp(options: Options) {
        val help = HelpFormatter()
        help.optionComparator = null
        help.printHelp(150, "GetBilibili.jar", "", options, "", true)
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class, NoSuchAlgorithmException::class)
    private fun generateLink(url: String) {
        if (url.contains("playurl") && url.contains("json")) {
            Link = parseJSON(url)
        } else if (url.contains("playurl")) {
            Link = parseXML(url)
        } else {
            getCID(url)
            getLink()
        }
    }

    @Throws(IOException::class)
    private fun createDirectory(parse: CommandLine) {
        val tempPath = System.getenv("APPDATA")
        TempDir = Paths.get(tempPath, "GetBilibili")
        if (parse.getOptionValue("dir") != null) {
            Dir = Paths.get(parse.getOptionValue("dir"), "GetBilibili")
        } else {
            val path = URLDecoder.decode(GetBilibili::class.java.protectionDomain.codeSource.location.path, "utf-8")
            Dir = Paths.get(path.substring(1, path.lastIndexOf('/') + 1), "GetBilibili")
        }
        if (Files.notExists(TempDir)) {
            Files.createDirectories(TempDir)
        }
        if (Files.notExists(Dir)) {
            Files.createDirectories(Dir)
        }
    }

    @Throws(IOException::class)
    private fun getCID(url: String) {
        val connection = URL(url).openConnection()
        connection.setRequestProperty("Cookie", Cookie)

        var bufferedReader: BufferedReader
        try {
            bufferedReader = BufferedReader(InputStreamReader(GZIPInputStream(connection.inputStream), "utf-8"))
        } catch (e: ZipException) {
            bufferedReader = BufferedReader(InputStreamReader(connection.inputStream, "utf-8"))
        }

        var x = 0
        var y = 0
        bufferedReader.useLines {
            it.forEach { s ->
                if (s.contains("<div class=\"msgbox\"><div class=\"content\"><a id='login-btn'>登录</a></div></div>")) {
                    throw IllegalArgumentException("此为隐藏视频，需要设置 Cookie。")
                }
                if (url.contains("video")) {
                    if (s.contains("cid=")) {
                        x = 1
                        Video_Cid = s.substring(s.indexOf("cid=") + 4, s.indexOf('&'))
                    }
                    if (s.contains("<h1 title=")) {
                        y = 1
                        val i = s.lastIndexOf("</h1>")
                        Video_Title = s.substring(s.lastIndexOf('>', i) + 1, i)
                    }
                    if (x == 1 && y == 1) {
//                        break
                    }
                } else if (url.contains("movie")) {
                    if (s.contains("cid=")) {
                        x = 1
                        Video_Cid = s.substring(s.indexOf("cid=") + 5, s.indexOf("cid=") + 13)
                    }
                    if (s.contains("pay_top_msg")) {
                        y = 1
                        val i = s.lastIndexOf("</div>")
                        Video_Title = s.substring(s.lastIndexOf('>', i) + 1, i)
                    }
                    if (x == 1 && y == 1) {
//                        break
                    }
                } else if (url.contains("anime")) {
                    if (s.contains("v-av-link")) {
                        x = 1
                        val i = s.lastIndexOf("</a>")
                        val aid = s.substring(s.lastIndexOf('>', i) + 3, i)
                        BufferedReader(InputStreamReader(GZIPInputStream(URL("http://www.bilibili.com/widget/getPageList?aid=" + aid).openStream()), "utf-8")).use { bufferedReader2 -> Video_Cid = JsonParser().parse(bufferedReader2).asJsonArray.get(0).asJsonObject.get("cid").asString }
                    }
                    if (s.contains("<h1 title=")) {
                        y = 1
                        val i = s.lastIndexOf("</h1>")
                        Video_Title = s.substring(s.lastIndexOf('>', i) + 1, i)
                    }
                    if (x == 1 && y == 1) {
//                        break
                    }
                }
            }
        }
        bufferedReader.close()
    }

    @Throws(IOException::class, NoSuchAlgorithmException::class)
    private fun getLink() {
        val SubLink = StringBuilder().append("appkey=").append(String(Base64.getDecoder().decode(Appkey))).append("&cid=").append(Video_Cid).append("&otype=json&quality=3&type=flv")
        val Sign = hash(SubLink.toString() + String(Base64.getDecoder().decode(Secretkey)), "MD5")
        Link = parseJSON("http://interface.bilibili.com/playurl?$SubLink&sign=$Sign")
    }

    private fun showLink() {
        val sizeFormat = DecimalFormat("0.00")
        val numFormat = DecimalFormat("0,000")
        val timeFormat = DecimalFormat("00")
        val s = Video_Length / 1000//秒
        val m = s / 60//分
        val h = m / 60//时
        System.out.printf("Title: %s\n", fileName + if (isConvert) ".mp4" else ".flv")
        System.out.printf("Total Size: %s MB (%s bytes)\tTotal Time: %s:%s:%s (%s:%s Mins)\n\n", sizeFormat.format(Video_Size / (1024 * 1024.0)), numFormat.format(Video_Size), timeFormat.format(h.toLong()), timeFormat.format((m % 60).toLong()), timeFormat.format((s % 60).toLong()), timeFormat.format(m.toLong()), timeFormat.format((s % 60).toLong()))
        Link!!.forEach(::println)
    }

    @Throws(IOException::class)
    private fun saveLink() {
        val link = TempDir!!.resolve("1.txt")
        link.toFile().deleteOnExit()
        Files.write(link, Link, Charset.forName("utf-8"))
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun downLoad() {
        if (Files.notExists(TempDir!!.resolve("aria2c.exe"))) {
            getEXE(Aria2Link)
        }
        execute(TempDir!!.resolve("aria2c.exe").toString(), "--input-file=1.txt", "--dir=" + Dir!!.toString(), "--disk-cache=32M", "--user-agent=" + UserAgent, "--enable-mmap=true", "--max-mmap-limit=2048M", "--continue=true", "--max-concurrent-downloads=1", "--max-connection-per-server=10", "--min-split-size=10M", "--split=10", "--disable-ipv6=true", "--http-no-cache=true", "--check-certificate=false")
    }

    @Throws(IOException::class)
    private fun listFile() {
        val fileList = TempDir!!.resolve("2.txt")
        fileList.toFile().deleteOnExit()

        val paths: List<File> = Dir!!.toFile().listFiles { dir, name -> name.endsWith(".flv") || name.endsWith(".mp4") }.sortedBy {
            val name1 = it.name
            val s1 = name1.substring(name1.indexOf("-") + 1, name1.indexOf("."))
            val pattern = Pattern.compile("\\d+")
            val matcher1 = pattern.matcher(s1)
            if (matcher1.find()) Integer.valueOf(matcher1.group()) else 0
        }

        val s = "file 'X'"
        PrintWriter(BufferedWriter(OutputStreamWriter(Files.newOutputStream(fileList), "utf-8"))).use { paths.forEach { o -> it.println(s.replace("X", o.toString())) } }
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun mergeFLV() {
        val tempFile = Dir!!.resolveSibling("123.flv")
        val finalFile = Dir!!.resolveSibling(fileName + if (isConvert) ".mp4" else ".flv")

        if (Link != null && Link!!.size == 1) {
            val s = Link!![0]
            val i = s.indexOf('?')
            val name = s.substring(s.lastIndexOf('/', i) + 1, i)
            Files.move(Dir!!.resolve(name), Dir!!.resolveSibling(fileName + name.substring(name.lastIndexOf('.'))), REPLACE_EXISTING)//移动文件至上层目录
        } else {
            println("\nMerging...")
            if (Files.notExists(TempDir!!.resolve("ffmpeg.exe"))) {
                getEXE(FFmpegLink)
            }
            execute(TempDir!!.resolve("ffmpeg.exe").toString(), "-f", "concat", "-safe", "-1", "-i", "2.txt", "-c", "copy", "-y", tempFile.toString())

            if (isConvert) {
                println("\nConverting...")
                execute(TempDir!!.resolve("ffmpeg.exe").toString(), "-i", tempFile.toString(), "-c", "copy", "-y", finalFile.toString())
            } else {
                println("\nMerging...")
                if (Files.notExists(TempDir!!.resolve("yamdi.exe"))) {
                    getEXE(YamdiLink)
                }
                execute(TempDir!!.resolve("yamdi.exe").toString(), "-i", tempFile.toString(), "-o", finalFile.toString())
            }
        }

        Files.deleteIfExists(tempFile)

        if (isDelete) {
            Dir!!.toFile().listFiles().forEach(File::deleteOnExit)
            Files.deleteIfExists(Dir!!)
        }
    }

    private val fileName: String
        get() {
            if (Video_Title == null) {
                return "Video"
            }
            val stringBuilder = StringBuilder()
            for (c in Video_Title!!.toCharArray()) {
                val unicodeBlock = Character.UnicodeBlock.of(c)
                stringBuilder.append(if (unicodeBlock == LATIN_1_SUPPLEMENT || unicodeBlock == KATAKANA) "" else c)
            }
            val s = stringBuilder.toString()
            return s.replace('/', ' ').replace('\\', ' ').replace(':', ' ').replace('*', ' ').replace('?', ' ').replace('"', ' ').replace('<', ' ').replace('>', ' ').replace('|', ' ').replace('‧', ' ').replace('•', ' ')
        }

    @Throws(IOException::class, InterruptedException::class)
    private fun getEXE(link: String) {
        if (Files.notExists(TempDir!!.resolve("7zr.exe"))) {
            getFile(SevenZipLink)
        }
        getFile(link)
        execute(TempDir!!.resolve("7zr.exe").toString(), "x", "-y", link.substring(link.lastIndexOf('/') + 1))
    }

    @Throws(IOException::class)
    private fun getFile(link: String) {
        val connection = URL(link).openConnection()
        connection.setRequestProperty("User-Agent", UserAgent)
        val path = TempDir!!.resolve(link.substring(link.lastIndexOf('/') + 1))
        path.toFile().deleteOnExit()

        connection.inputStream.use { inputStream -> Files.copy(inputStream, path, REPLACE_EXISTING) }
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun execute(vararg command: String) {
        val process = ProcessBuilder(*command).directory(TempDir!!.toFile()).redirectErrorStream(true).redirectOutput(Redirect.INHERIT).redirectInput(Redirect.INHERIT).start()
        Tasks.add(process)
        process.waitFor()
        Tasks.remove(process)
    }

    @Throws(NoSuchAlgorithmException::class)
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

    @Throws(IOException::class)
    private fun parseJSON(link: String): List<String> {
        val Link = ArrayList<String>()
        val connection = URL(link).openConnection()
        connection.setRequestProperty("Cookie", Cookie)

        BufferedReader(InputStreamReader(connection.inputStream, "utf-8")).use { bufferedReader ->
            val jsonObject = JsonParser().parse(bufferedReader).asJsonObject
            val jsonArray = jsonObject.getAsJsonArray("durl")
            jsonArray.forEach { durl ->
                val durlObject = durl.asJsonObject
                Link.add(durlObject.get("url").asString)
                Video_Size += durlObject.get("size").asInt.toLong()
                Video_Length += durlObject.get("length").asInt
            }
        }
        return Link
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    private fun parseXML(link: String): List<String> {
        val connection = URL(link).openConnection()
        connection.setRequestProperty("Cookie", Cookie)
        val xml: String = BufferedReader(InputStreamReader(connection.inputStream, "utf-8")).useLines {
            val stringBuilder = StringBuilder()
            it.forEach { stringBuilder.append(it) }
            stringBuilder.toString()
        }/*XML 文件*/

        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = documentBuilder.parse(InputSource(StringReader(xml)))/*XML 对象*/

        val Link = ArrayList<String>()
        val durl = document.getElementsByTagName("durl")
        for (i in 0..durl.length - 1) {
            val element = durl.item(i) as Element
            Link.add(element.getElementsByTagName("url").item(0).firstChild.nodeValue)
            Video_Size += java.lang.Long.valueOf(element.getElementsByTagName("size").item(0).firstChild.nodeValue)!!
            Video_Length += Integer.valueOf(element.getElementsByTagName("length").item(0).firstChild.nodeValue)!!
        }
        return Link
    }
}
