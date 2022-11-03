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
    classpath "net.xdob.pf4boot:pf4boot-plugin:0.3.0"
  }
}

apply plugin: 'net.xdob.pf4boot-plugin'
```
## 插件配置支持两种方式,任选其一即可
1. 模块根目录添加plugin.properties配置
```properties
plugin.id=plugin1
plugin.class=net.xdob.demo.plugin1.Plugin1Plugin
plugin.provider=yangzj
plugin.dependencies=
plugin.description=
plugin.requires=
plugin.license=
```
2. build.gradle中添加配置项
```groovy
pf4bootPlugin{
    id='plugin2'
    pluginClass='net.xdob.demo.plugin2.Plugin2Plugin'
    provider='yangzj'
    dependencies='plugin1'
    description=''
}
```


