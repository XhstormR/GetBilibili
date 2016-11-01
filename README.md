# GetBilibili
![](http://ww4.sinaimg.cn/large/a15b4afegw1f80cg69rtpg20sp0ehte3 "GetBilibili")

## Link
Get Bilibili Ultra-Definition Video Link
```bash
GetBilibili.jar -l http://www.bilibili.com/video/av6896579/
```

## Download
Download Bilibili Ultra-Definition Video
```bash
GetBilibili.jar -d http://www.bilibili.com/video/av6896579/ 1 D:/TEST     下载且合并完成后，转为 MP4 格式，下载目录指定为 D:/TEST
GetBilibili.jar -d http://www.bilibili.com/video/av6896579/ 0 0     下载且合并完成后，保留为原格式，下载目录为原 JAR 包位置
GetBilibili.jar -d http://www.bilibili.com/video/av6896579/     下载且合并完成后，保留为原格式，下载目录为原 JAR 包位置（默认）
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

GetBilibili.jar -x "https://interface.bilibili.com/playurl?cid=11239800&ts=1477907912&player=1&sign=d47cc63f6ca756e3d9b806b7068df18b"
```

## Merge
Merge Segmented Video
```bash
新建 GetBilibili 文件夹，在里面放置 FLV 文件即可

GetBilibili.jar -m 1 1     （参数2）合并后删除源文件，（参数3）转为 MP4 格式
GetBilibili.jar -m 0 0     （参数2）合并后保留源文件，（参数3）保留为原格式
GetBilibili.jar -m     合并后删除源文件，保留为原格式（默认）
```

## Note
现在的这个 Key 因使用次数过多而被限速了（影响 `-d` 选项，10 KB/S），也不知道多久能恢复。

而我也不打算找更多的 Key 了，毕竟这不是可持续性发展。

所以我增加了 JSON 和 XML 这 2 种下载方式，算是一种 workaround 吧。

## Reference
https://www.v2ex.com/t/307373

JRE：https://pan.baidu.com/s/1i5nt6AT 密码：nubs

## TODO
- [x] 显示视频的标题、大小和时间
- [x] 保存文件名为视频标题
- [x] 自定义下载目录
- [x] 合并后无损转为 MP4 格式
- [x] 支持解析番剧视频
- [x] 支持解析隐藏视频（需自行设置 Cookie）
- [x] 支持解析 1080P 分辨率视频（需自行设置 Cookie）
