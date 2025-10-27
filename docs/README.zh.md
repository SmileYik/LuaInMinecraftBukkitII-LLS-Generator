# LuaInMinecraftBukkitII-LLS-Generator

这个仓库意在为 [LuaInMinecraftBukkitII] 插件生成Bukkit/Paper API和Java标准库API桩文件, 以让 [Lua Language Server] 可以读取并实现Lua代码自动补全.

## 功能

* 读取 Java 源代码生成 [LuaLS][Lua Language Server] 可读的Lua桩文件
* 自动下载Maven仓库中的源代码jar包.

## 如何使用

* 确保你在一开始就安装了JDK!
* 记得克隆这个仓库!

### 生成Java标准库API桩文件

如果你想为 [LuaLS][Lua Language Server] 生成Java标准库API的桩文件的话, 仅需要执行以下代码:

```shell
./gradlew generateByJavaStandardApi
```

after that, you can find your [LuaLS][Lua Language Server] Lua stub file at folder: `./lua/java-standard-api`

### 生成PaperAPI桩文件

如果你想为 [LuaLS][Lua Language Server] 生成 [Paper API][PaperMC] 的桩文件的话, 仅需要执行以下代码:

```shell
./gradlew generateByPaperMaven
```

### 对于其他Java依赖库桩文件

如果你的依赖库能够在 maven 仓库中访问, 并且在仓库中含有源代码jar包, 那么你就可以通过编辑 [paper-maven.json] 和 [build.gradle] 来实现生成.

[LuaInMinecraftBukkitII]: https://github.com/SmileYik/LuaInMinecraftBukkitII
[Lua Language Server]: https://luals.github.io/
[PaperMC]: https://papermc.io/
[paper-maven.json]: ../paper-maven.json
[build.gradle]: ../build.gradle