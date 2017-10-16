import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class GetBilibili {
    private static final String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36";
    private static final String Aria2Link = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vk9216gvj203k03kb29";
    private static final String YamdiLink = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhnqa9soj203k03kwfe";
    private static final String FFmpegLink = "http://ww4.sinaimg.cn/large/a15b4afegw1f7vhtyv0wbj203k03k4qz";
    private static final String SevenZipLink = "http://blog.xhstormr.tk/uploads/bin/7zr.exe";
    private static final String Appkey = "85eb6835b0a1034e";
    private static final String Secretkey = "2ad42749773c441109bdc0191257a664";
    private static File Dir;
    private static String Cid;
    private static List<String> Link;
    private static boolean Delete = true;

    public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException {
        String path = GetBilibili.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        Dir = new File(path.substring(0, path.lastIndexOf('/')), "GetBilibili");
        if (!Dir.exists()) {
            Dir.mkdir();
        }

        System.out.println();
        if (args.length < 2) {
            System.out.println("Error!!!");
            return;
        } else switch (args[0]) {
            case "-l":
                getCID(args[1]);
                getLink();
                Link.forEach(System.out::println);
                break;
            case "-m":
                Delete = args[1].equals("1");
                listFile();
                mergeFLV();
                break;
            case "-d":
                getCID(args[1]);
                getLink();
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

    private static void getCID(String url) throws IOException {
        /*Jsoup 解析页面获得 Cid*/
//        Document doc = Jsoup.connect(url).userAgent(UserAgent).get();
//        Element player = doc.getElementsByClass("scontent").select("script").get(0);
//        String data = player.data();
//        Cid = data.substring(data.indexOf("cid") + 4, data.indexOf('&'));

        /*原生解析页面获得 Cid*/
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new URL(url).openStream()), "utf-8"));
//        Scanner scanner = new Scanner(bufferedReader);
//        for (String s; scanner.hasNextLine(); ) {
//            s = scanner.nextLine();
//            if (s.contains("cid=")) {
//                bufferedReader.close();
//                Cid = s.substring(s.indexOf("cid=") + 4, s.indexOf('&'));
//                break;
//            }
//        }
//        bufferedReader.close();

        /*原生解析页面获得 Cid*/
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new URL(url).openStream()), "utf-8"));
        for (String s; (s = bufferedReader.readLine()) != null; ) {
            if (s.contains("cid=")) {
                bufferedReader.close();
                Cid = s.substring(s.indexOf("cid=") + 4, s.indexOf('&'));
                break;
            }
        }
        bufferedReader.close();
    }

    private static void getLink() throws IOException, NoSuchAlgorithmException {
        /*通过 Jsoup 解析第三方 BiliBiliJJ 获得 Link*/
//        String url = "http://www.bilibilijj.com/DownLoad/Cid/" + Cid;
//        Document doc = Jsoup.connect(url).userAgent(UserAgent).get();
//        Link = new ArrayList<>();
//        if (doc.baseUri().contains("DownLoad")) {
//            Elements elements = doc.getElementsByClass("D").select("a[href]");
//            for (Element element : elements) {
//                Link.add(element.attr("href"));
//            }
//        } else if (doc.baseUri().contains("FreeDown")) {
//            Link.add(doc.getElementsByClass("putong").parents().get(0).attr("href"));
//        }

        /*通过 JSON 解析官方 BiliBili 获得 Link*/
        StringBuilder SubLink = new StringBuilder().append("appkey=").append(Appkey).append("&cid=").append(Cid).append("&otype=json&quality=3&type=flv");
        String Sign = Hash(SubLink + Secretkey, "MD5");
        Link = Json("http://interface.bilibili.com/playurl?" + SubLink + "&sign=" + Sign);
    }

    private static void saveLink() throws IOException {
        File link = new File(Dir, "1.txt");
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
        if (!new File(Dir, "aria2c.exe").exists()) {
            getEXE(Aria2Link);
        }
        execute(true, Dir.getAbsolutePath() + "/aria2c.exe", "--input-file=1.txt", "--dir=Download", "--disk-cache=32M", "--enable-mmap=true", "--max-mmap-limit=2048M", "--continue=true", "--max-concurrent-downloads=1", "--max-connection-per-server=10", "--min-split-size=10M", "--split=10", "--disable-ipv6=true", "--http-no-cache=true", "--check-certificate=false");
    }

    private static void listFile() throws IOException {
        File fileList = new File(Dir, "2.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileList, false), "utf-8"));
        File downLoadDir = new File(Dir, "Download");
        File[] files = downLoadDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".flv");
            }
        });

        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    String name1 = o1.getName();
                    String name2 = o2.getName();
                    String s1 = name1.substring(name1.indexOf("-") + 1, name1.indexOf("."));
                    String s2 = name2.substring(name2.indexOf("-") + 1, name2.indexOf("."));
                    return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
                }
            });
            for (File file : files) {
                String name = file.getName();
                bufferedWriter.write(new StringBuilder("file 'Download/'").insert(15, name).toString());
                bufferedWriter.newLine();
            }
        }
        bufferedWriter.close();
        if (fileList.exists()) {
            fileList.deleteOnExit();
        }
    }

    private static void mergeFLV() throws IOException, InterruptedException {
        /*方案一*/
//        String s1 = "cmd.exe /C start /W D:/ffmpeg.exe -f concat -i D:/2.txt -c copy D:/12.flv";
//        Process exec1 = Runtime.getRuntime().exec(s1);
//        exec1.waitFor();
//        String s2 = "cmd.exe /C start /W D:/yamdi.exe -i D:/12.flv -o D:/123.flv";
//        Process exec2 = Runtime.getRuntime().exec(s2);
//        exec2.waitFor();

        /*方案二*/
        if (!new File(Dir, "ffmpeg.exe").exists()) {
            getEXE(FFmpegLink);
        }
        execute(true, Dir.getAbsolutePath() + "/ffmpeg.exe", "-f", "concat", "-i", "2.txt", "-c", "copy", "123.flv");

        if (!new File(Dir, "yamdi.exe").exists()) {
            getEXE(YamdiLink);
        }
        execute(true, Dir.getAbsolutePath() + "/yamdi.exe", "-i", "123.flv", "-o", Dir.getParent() + "/" + (Cid != null ? Cid : "Video") + ".flv");

        File file = new File(Dir, "123.flv");
        if (file.exists()) {
            file.deleteOnExit();
        }

        if (Delete) {
            File downLoadDir = new File(Dir, "Download");
            File[] files = downLoadDir.listFiles();
            if (files != null) {
                for (File flv : files) {
                    flv.delete();
                }
            }
            downLoadDir.deleteOnExit();
        }
    }

    private static void getEXE(String link) throws IOException, InterruptedException {
        if (!new File(Dir, "7zr.exe").exists()) {
            getFile(SevenZipLink);
        }
        File file = getFile(link);
        execute(false, Dir.getAbsolutePath() + "/7zr.exe", "x", file.getName());
        if (file.exists()) {
            file.delete();
        }
    }

    private static File getFile(String link) throws IOException {
        URL url = new URL(link);
        URLConnection connection = url.openConnection();
        connection.addRequestProperty("User-Agent", UserAgent);
        String path = url.getFile();
        File file = new File(Dir, path.substring(path.lastIndexOf('/') + 1));

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
        Process process = new ProcessBuilder(command).directory(Dir).redirectErrorStream(true).start();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "gbk"));
        for (String s; (s = bufferedReader.readLine()) != null; ) {
            if (show) {
                System.out.println(s);
            }
        }
        process.waitFor();
        bufferedReader.close();
    }

    private static String Hash(String str, String algorithm) throws NoSuchAlgorithmException {
        /*方案一*/
//        MessageDigest instance = MessageDigest.getInstance(algorithm);
//        return new BigInteger(1, instance.digest(str.getBytes())).toString(16);//10->16

        /*方案二*/
        MessageDigest instance = MessageDigest.getInstance(algorithm);
        byte[] digest = instance.digest(str.getBytes());

        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : digest) {
            String s = Integer.toHexString(b & 0xff);
            stringBuilder = s.length() < 2 ? stringBuilder.append('0').append(s) : stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    private static List<String> Json(String link) throws IOException {
        /*通过自建 Bean 对象解析*/
//        List<String> Link = new ArrayList<>();
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(link).openStream(), "utf-8"));
//        Video video = new Gson().fromJson(bufferedReader, Video.class);
//        for (Url url : video.getDurl()) {
//            Link.add(url.getUrl());
//        }
//        bufferedReader.close();
//        return Link;

        /*通过自带 Bean 对象解析*/
        List<String> Link = new ArrayList<>();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(link).openStream(), "utf-8"));
        JsonObject jsonObject = new JsonParser().parse(bufferedReader).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("durl");
        for (JsonElement durl : jsonArray) {
            Link.add(durl.getAsJsonObject().get("url").getAsString());
        }
        bufferedReader.close();
        return Link;
    }
}
