# pf4boot-plugin
pf4boot插件开发的gradle插件

## gradle插件引用
```groovy
buildscript {
  repositories {
    mavenLocal()
    maven { url 'https://maven.aliyun.com/repository/public/' }
    mavenCentral()
  }
  dependencies {
    classpath "net.xdob.pf4boot:pf4boot-plugin:0.5.0"
  }
}

apply plugin: 'net.xdob.pf4boot-plugin'
```
## 插件配置
插件模块根目录添加plugin.properties配置
```properties
plugin.id=plugin1
plugin.class=net.xdob.demo.plugin1.Plugin1Plugin
plugin.provider=yangzj
plugin.dependencies=
plugin.description=
plugin.requires=
plugin.license=
```



