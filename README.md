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
GetBilibili.jar -d http://www.bilibili.com/video/av1585238 1 D:/TEST     下载且合并完成后，转为MP4格式，文件下载目录指定为 D:/TEST
GetBilibili.jar -d http://www.bilibili.com/video/av1585238 0 0     下载且合并完成后，保留为原格式，文件下载目录为原 JAR 包位置
GetBilibili.jar -d http://www.bilibili.com/video/av1585238     下载且合并完成后，保留为原格式，文件下载目录为原 JAR 包位置（默认）
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
