﻿# fuckewt-java

# 用法

建议写成bat文件防止崩溃

确保当前文件夹下存在`MicrosoftWebDriver.exe`

```bat
@echo off
:loop
java -jar selenium-fuckewt.jar <用户> <密码> <headless/head>
goto loop
```
