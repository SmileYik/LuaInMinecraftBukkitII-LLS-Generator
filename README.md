# LuaInMinecraftBukkitII-LLS-Generator

[中文][Readme-zh]

This repository aims to generate [LuaInMinecraftBukkitII] API, Bukkit API and Java Standard API for the [Lua Language Server].

## Feature

* Read Java source file and generate [LuaLS][Lua Language Server] Lua stub file
* Automatic download sources jar file from maven repository.

## How to use

* Make sure you installed JDK at first!
* Clone this repository

### For Java standard API

If you want to generate [LuaLS][Lua Language Server] Lua stub file of Java standard API, just executes command bellow:

```shell
./gradlew generateByJavaStandardApi
```

after that, you can find your [LuaLS][Lua Language Server] Lua stub file at folder: `./lua/java-standard-api`

### For Paper API

If you want to generate [LuaLS][Lua Language Server] Lua stub file of [Paper API][PaperMC], just executes command bellow:

```shell
./gradlew generateByPaperMaven
```

### For other java libraries

If your java libraries can be access from maven repository and has sources jar file in maven repository,
then you can configure [paper-maven.json] and [build.gradle].

[LuaInMinecraftBukkitII]: https://github.com/SmileYik/LuaInMinecraftBukkitII
[Lua Language Server]: https://luals.github.io/
[PaperMC]: https://papermc.io/
[paper-maven.json]: ./paper-maven.json
[build.gradle]: ./build.gradle
[Readme-zh]: ./docs/README.zh.md