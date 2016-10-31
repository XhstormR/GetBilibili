# GetBilibili
![](http://ww4.sinaimg.cn/large/a15b4afegw1f80cg69rtpg20sp0ehte3 "GetBilibili")

## Link
Get Bilibili Ultra-Definition Video Link
```bash
GetBilibili.jar -l http://www.bilibili.com/video/av1585238
```

## Download
Download Bilibili Ultra-Definition Video
```bash
GetBilibili.jar -d http://www.bilibili.com/video/av1585238 1 D:/TEST     下载且合并完成后，转为MP4格式，下载目录指定为 D:/TEST
GetBilibili.jar -d http://www.bilibili.com/video/av1585238 0 0     下载且合并完成后，保留为原格式，下载目录为原 JAR 包位置
GetBilibili.jar -d http://www.bilibili.com/video/av1585238     下载且合并完成后，保留为原格式，下载目录为原 JAR 包位置（默认）
```

### JSON
Download Bilibili Ultra-Definition Video via JSON
```bash
下载且合并完成后，保留为原格式，下载目录为原 JAR 包位置

GetBilibili.jar -j "https://interface.bilibili.com/playurl?cid=11239800&appkey=84956560bc028eb7&otype=json&type=flv&quality=3&sign=c639a8283b4180cf0c3d553de3387309"
```

### XML
Download Bilibili Ultra-Definition Video via XML
```bash
下载且合并完成后，保留为原格式，下载目录为原 JAR 包位置

GetBilibili.jar -x "https://interface.bilibili.com/playurl?cid=11239800&sign=786f33c637b49b8af46124c2ff64d654&ts=1477907112&player=1"
```

## Merge
Merge Segmented Video
```bash
新建 GetBilibili 文件夹，在里面放置 FLV 文件即可

GetBilibili.jar -m 1 1     （参数2）合并后删除源文件，（参数3）转为 MP4 格式
GetBilibili.jar -m 0 0     （参数2）合并后保留源文件，（参数3）保留为原格式
GetBilibili.jar -m     合并后删除源文件，保留为原格式（默认）
```

## Reference
https://www.v2ex.com/t/307373

JRE：https://pan.baidu.com/s/1i5nt6AT 密码：nubs

## TODO
- [x] 显示视频的标题、大小和时间
- [x] 保存文件名为视频标题
- [x] 自定义文件下载目录
- [x] 合并后无损转为 MP4 格式
- [x] 支持解析隐藏视频
- [x] 支持解析番剧视频
