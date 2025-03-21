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
    classpath "net.xdob.pf4boot:pf4boot-plugin:1.0.0"
  }
}
```

## pf4boot应用配置方法
pf4boot应用的build.gradle配置
```groovy
//引用 pf4boot插件
apply plugin: 'net.xdob.pf4boot'
```

## 插件配置方法1
插件模块根目录添加plugin.properties配置
```properties
plugin.id=plugin1
plugin.class=net.xdob.demo.plugin1.Plugin1Plugin
#plugin.version=0.1.0-SNAPSHOT
plugin.provider=yangzj
plugin.dependencies=
plugin.description=
plugin.requires=
plugin.license=
```

## 插件配置方法2
插件模块的build.gradle配置
```groovy
pf4bootPlugin{
    id = 'plugin1'
    pluginClass = 'net.xdob.demo.plugin1.Plugin1Plugin'
    version = '0.1.0-SNAPSHOT'
    provider = 'yangzj'
    dependencies = ''
    description = ''
    requires = ''
    license = ''
}
```

