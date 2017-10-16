import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sun.misc.BASE64Decoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import static java.lang.Character.UnicodeBlock.LATIN_1_SUPPLEMENT;

public class GetBilibili {
    private static final String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36";
    private static final String Aria2Link = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vk9216gvj203k03kb29";
    private static final String YamdiLink = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhnqa9soj203k03kwfe";
    private static final String FFmpegLink = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhtyv0wbj203k03k4qz";
    private static final String SevenZipLink = "http://blog.xhstormr.tk/uploads/bin/7zr.exe";
    private static final String Appkey = "NmY5MGE1OWFjNThhNDEyMw==";
    private static final String Secretkey = "MGJmZDg0Y2MzOTQwMDM1MTczZjM1ZTY3Nzc1MDgzMjY=";
    private static final String Cookie = "DedeUserID=1424743; DedeUserID__ckMd5=472e7fe30d4f15eb; SESSDATA=f204dbc8%2C1E88438047%2Cfe76287b; sid=9y6y864j";
    private static File Dir;
    private static File TempDir;
    private static String Video_Cid;
    private static String Video_Title;
    private static long Video_Size;
    private static int Video_Length;
    private static List<String> Link;
    private static boolean isDelete = true;
    private static boolean isConvert = false;
    private static Set<Process> Tasks = new HashSet<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> Tasks.forEach(Process::destroyForcibly)));
    }

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, ParserConfigurationException, SAXException {
        System.out.println();
        if (args.length < 1) {
            System.out.println("Error!!!");
            return;
        } else switch (args[0]) {
            case "-l":
                if (args.length < 2) {
                    System.out.println("Error!!!");
                    return;
                }
                getCID(args[1]);
                getLink();
                DecimalFormat sizeFormat = new DecimalFormat("0.00");
                DecimalFormat numFormat = new DecimalFormat("0,000");
                DecimalFormat timeFormat = new DecimalFormat("00");
                int s = Video_Length / 1000;
                System.out.println("Title: " + Video_Title);
                System.out.println("Total Size: " + sizeFormat.format(Video_Size / (1024 * 1024.0)) + " MB (" + numFormat.format(Video_Size) + " bytes)\t" + "Total Time: " + timeFormat.format(s / 60) + ":" + timeFormat.format(s % 60) + " Mins\n");
                Link.forEach(System.out::println);
                break;
            case "-m":
                if (args.length > 2) {
                    isDelete = args[1].equals("1");
                    isConvert = args[2].equals("1");
                }
                createDirectory();
                listFile();
                mergeFLV();
                break;
            case "-d":
                if (args.length < 2) {
                    System.out.println("Error!!!");
                    return;
                }
                createDirectory();
                getCID(args[1]);
                if (args.length > 3) {
                    isConvert = args[2].equals("1");
                    if (!args[3].equals("0")) {
                        Dir.delete();
                        Dir = new File(args[3], "GetBilibili");
                        if (!Dir.exists()) {
                            Dir.mkdirs();
                        }
                    }
                }
                getLink();
                saveLink();
                downLoad();
                listFile();
                mergeFLV();
                break;
            case "-j":
                createDirectory();
                Link = parseJSON(args[1]);
                saveLink();
                downLoad();
                listFile();
                mergeFLV();
                break;
            case "-x":
                createDirectory();
                Link = parseXML(args[1]);
                saveLink();
                downLoad();
                listFile();
                mergeFLV();
                break;
            default:
                System.out.println("Error!!!");
                return;
        }
        System.out.println("\n" + "Done!!!");
    }

    private static void createDirectory() throws UnsupportedEncodingException {
        String path = URLDecoder.decode(GetBilibili.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "utf-8");
        Dir = new File(path.substring(0, path.lastIndexOf('/')), "GetBilibili");
        if (!Dir.exists()) {
            Dir.mkdirs();
        }
        String tempPath = System.getenv("APPDATA");
        TempDir = new File(tempPath, "GetBilibili");
        if (!TempDir.exists()) {
            TempDir.mkdirs();
        }
    }

    private static void getCID(String url) throws IOException {
        if (url.contains("bangumi")) {
            if (url.contains("movie")) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                for (String s; (s = bufferedReader.readLine()) != null; ) {
                    if (s.contains("formalAid")) {
                        int i = s.indexOf('"') + 1;
                        int j = s.indexOf('"', i);
                        url = new StringBuilder("http://www.bilibili.com/video/av/").insert(32, s.substring(i, j)).toString();
                        break;
                    }
                }
                bufferedReader.close();
            }
            if (url.contains("anime")) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
                for (String s; (s = bufferedReader.readLine()) != null; ) {
                    if (s.contains("v-av-link")) {
                        int i = s.indexOf('"') + 1;
                        int j = s.indexOf('"', i);
                        url = s.substring(i, j);
                        break;
                    }
                }
                bufferedReader.close();
            }
        }

        URLConnection connection = new URL(url).openConnection();
        connection.setRequestProperty("Cookie", Cookie);
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), "utf-8"));
        } catch (ZipException e) {
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        }
        int x = 0, y = 0;
        boolean isBangumi = false;
        for (String s; (s = bufferedReader.readLine()) != null; ) {
            if (s.contains("<!-- aid，价格， 没有背景图的时候：0 其它：1-->")) {
                isBangumi = true;
            }
            if (!isBangumi) {
                if (s.contains("cid=")) {
                    x = 1;
                    Video_Cid = s.substring(s.indexOf("cid=") + 4, s.indexOf('&'));
                }
                if (s.contains("<h1 title=")) {
                    y = 1;
                    int i = s.lastIndexOf("</h1>");
                    Video_Title = s.substring(s.lastIndexOf('>', i) + 1, i);
                }
                if (x == 1 && y == 1) {
                    break;
                }
            } else {
                if (s.contains("cid=")) {
                    x = 1;
                    Video_Cid = s.substring(s.indexOf("cid=") + 5, s.indexOf("cid=") + 13);
                }
                if (s.contains("pay_top_msg")) {
                    y = 1;
                    int i = s.lastIndexOf("</div>");
                    Video_Title = s.substring(s.lastIndexOf('>', i) + 1, i);
                }
                if (x == 1 && y == 1) {
                    break;
                }
            }
        }
        bufferedReader.close();
    }

    private static void getLink() throws IOException, NoSuchAlgorithmException {
        StringBuilder SubLink = new StringBuilder().append("appkey=").append(new String(new BASE64Decoder().decodeBuffer(Appkey), "utf-8")).append("&cid=").append(Video_Cid).append("&otype=json&quality=3&type=flv");
        String Sign = hash(SubLink + new String(new BASE64Decoder().decodeBuffer(Secretkey), "utf-8"), "MD5");
        Link = parseJSON("http://interface.bilibili.com/playurl?" + SubLink + "&sign=" + Sign);
    }

    private static void saveLink() throws IOException {
        File link = new File(TempDir, "1.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(link, false), "utf-8"));
        for (String s : Link) {
            bufferedWriter.write(s);
            bufferedWriter.newLine();
        }
        bufferedWriter.close();
        if (link.exists()) {
            link.deleteOnExit();
        }
    }

    private static void downLoad() throws IOException, InterruptedException {
        if (!new File(TempDir, "aria2c.exe").exists()) {
            getEXE(Aria2Link);
        }
        execute(true, TempDir.getAbsolutePath() + "/aria2c.exe", "--input-file=1.txt", "--dir=" + Dir.getAbsolutePath(), "--disk-cache=32M", "--enable-mmap=true", "--max-mmap-limit=2048M", "--continue=true", "--max-concurrent-downloads=1", "--max-connection-per-server=10", "--min-split-size=10M", "--split=10", "--disable-ipv6=true", "--http-no-cache=true", "--check-certificate=false");
    }

    private static void listFile() throws IOException {
        File fileList = new File(TempDir, "2.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileList, false), "utf-8"));
        File[] files = Dir.listFiles((dir, name) -> name.endsWith(".flv") || name.endsWith(".mp4"));

        if (files != null) {
            Arrays.sort(files, (o1, o2) -> {
                String name1 = o1.getName();
                String name2 = o2.getName();
                String s1 = name1.substring(name1.indexOf("-") + 1, name1.indexOf("."));
                String s2 = name2.substring(name2.indexOf("-") + 1, name2.indexOf("."));

                Pattern pattern = Pattern.compile("\\d+");

                Matcher matcher1 = pattern.matcher(s1);
                Matcher matcher2 = pattern.matcher(s2);
                Integer i1 = matcher1.find() ? Integer.valueOf(matcher1.group()) : 0;
                Integer i2 = matcher2.find() ? Integer.valueOf(matcher2.group()) : 0;
                return i1.compareTo(i2);
            });
            for (File file : files) {
                String path = file.getAbsolutePath();
                bufferedWriter.write(new StringBuilder("file ''").insert(6, path).toString());
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.close();
        if (fileList.exists()) {
            fileList.deleteOnExit();
        }
    }

    private static void mergeFLV() throws IOException, InterruptedException {
        File tempFLV = new File(Dir.getParent(), "123.flv");
        String finalFilePath = Dir.getParent() + "\\" + getFileName() + (isConvert ? ".mp4" : ".flv");

        System.out.println("\n" + "Merging...");
        if (!new File(TempDir, "ffmpeg.exe").exists()) {
            getEXE(FFmpegLink);
        }
        execute(true, TempDir.getAbsolutePath() + "/ffmpeg.exe", "-f", "concat", "-safe", "-1", "-i", "2.txt", "-c", "copy", tempFLV.getAbsolutePath());

        if (isConvert) {
            System.out.println("\n" + "Converting...");
            execute(true, TempDir.getAbsolutePath() + "/ffmpeg.exe", "-i", tempFLV.getAbsolutePath(), "-c", "copy", finalFilePath);
        } else {
            System.out.println("\n" + "Merging...");
            if (!new File(TempDir, "yamdi.exe").exists()) {
                getEXE(YamdiLink);
            }
            execute(true, TempDir.getAbsolutePath() + "/yamdi.exe", "-i", tempFLV.getAbsolutePath(), "-o", finalFilePath);
        }

        if (tempFLV.exists()) {
            tempFLV.deleteOnExit();
        }

        if (isDelete) {
            File[] files = Dir.listFiles();
            if (files != null) {
                for (File flv : files) {
                    flv.delete();
                }
            }
            Dir.deleteOnExit();
        }
    }

    private static String getFileName() {
        if (Video_Title == null) {
            return "Video";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : Video_Title.toCharArray()) {
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(c);
            stringBuilder.append(unicodeBlock.equals(LATIN_1_SUPPLEMENT) ? "" : c);
        }
        String s = stringBuilder.toString();
        return s.replace('/', ' ').replace('\\', ' ').replace(':', ' ').replace('*', ' ').replace('?', ' ').replace('"', ' ').replace('<', ' ').replace('>', ' ').replace('|', ' ').replace('‧', ' ').replace('•', ' ');
    }

    private static void getEXE(String link) throws IOException, InterruptedException {
        if (!new File(TempDir, "7zr.exe").exists()) {
            getFile(SevenZipLink);
        }
        File file = getFile(link);
        execute(false, TempDir.getAbsolutePath() + "/7zr.exe", "x", file.getName());
        if (file.exists()) {
            file.delete();
        }
    }

    private static File getFile(String link) throws IOException {
        URL url = new URL(link);
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", UserAgent);
        String path = url.getFile();
        File file = new File(TempDir, path.substring(path.lastIndexOf('/') + 1));

        BufferedInputStream bufferedInputStream = new BufferedInputStream(connection.getInputStream(), 32 * 1024);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file, false), 32 * 1024);
        byte[] bytes = new byte[32 * 1024];

        for (int read; (read = bufferedInputStream.read(bytes)) != -1; ) {
            bufferedOutputStream.write(bytes, 0, read);
        }

        bufferedInputStream.close();
        bufferedOutputStream.close();
        return file;
    }

    private static void execute(boolean show, String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).directory(TempDir).redirectErrorStream(true).start();
        Tasks.add(process);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
        for (String s; (s = bufferedReader.readLine()) != null; ) {
            if (show) {
                System.out.println(s);
            }
        }
        process.waitFor();
        Tasks.remove(process);
        bufferedReader.close();
    }

    private static String hash(String str, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest instance = MessageDigest.getInstance(algorithm);
        byte[] digest = instance.digest(str.getBytes());

        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : digest) {
            String s = Integer.toHexString(b & 0xff);
            stringBuilder = s.length() < 2 ? stringBuilder.append('0').append(s) : stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    private static List<String> parseJSON(String link) throws IOException {
        List<String> Link = new ArrayList<>();
        URLConnection connection = new URL(link).openConnection();
        connection.setRequestProperty("Cookie", Cookie);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        JsonObject jsonObject = new JsonParser().parse(bufferedReader).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("durl");
        for (JsonElement durl : jsonArray) {
            JsonObject durlObject = durl.getAsJsonObject();
            Link.add(durlObject.get("url").getAsString());
            Video_Size += durlObject.get("size").getAsInt();
            Video_Length += durlObject.get("length").getAsInt();
        }
        bufferedReader.close();
        return Link;
    }

    private static List<String> parseXML(String link) throws IOException, ParserConfigurationException, SAXException {
        URLConnection connection = new URL(link).openConnection();
        connection.setRequestProperty("Cookie", Cookie);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        StringBuilder stringBuilder = new StringBuilder();
        for (String s; (s = bufferedReader.readLine()) != null; ) {
            stringBuilder.append(s);
        }
        bufferedReader.close();
        String xml = stringBuilder.toString();/*XML 文件*/

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));/*XML 对象*/

        List<String> Link = new ArrayList<>();
        NodeList durl = document.getElementsByTagName("durl");
        for (int i = 0; i < durl.getLength(); i++) {
            Element element = (Element) durl.item(i);
            Link.add(element.getElementsByTagName("url").item(0).getFirstChild().getNodeValue());
        }
        return Link;
    }
}
