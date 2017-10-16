# GetBilibili
![](http://ww4.sinaimg.cn/large/a15b4afegw1f7z7kvlzcmg20sp0ehjw3 "GetBilibili")

## Link
Get Bilibili Ultra-Definition Video Link
```bash
GetBilibili.jar -l http://www.bilibili.com/video/av1585238
```

## Download
Download Bilibili Ultra-Definition Video
```bash
GetBilibili.jar -d http://www.bilibili.com/video/av1585238
```

## Merge
Merge Segmented Video
```bash
新建 GetBilibili 文件夹，在里面放置 FLV 文件即可

GetBilibili.jar -m 1     #合并后删除源文件
GetBilibili.jar -m 0     #合并后保留源文件
```

## Reference
https://www.v2ex.com/t/307373

## TODO
- [x] 显示视频的标题、大小和时间
- [x] 保存文件名为视频标题
- [ ] 自定义文件下载目录
- [ ] 合并后转为 MP4 格式
