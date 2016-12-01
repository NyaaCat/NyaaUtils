## nyaautils

Gaming utilities/helpers for NyaaCat Minecraft Server

Detailed function manual please refer to [Wiki](https://github.com/NyaaCat/nyaautils/wiki).

[![Build Status](https://travis-ci.org/NyaaCat/nyaautils.svg?branch=master)](https://travis-ci.org/NyaaCat/nyaautils)

## Use as dependency in Gradle

```
repositories {
    maven {
        name 'nyaa'
        url 'https://raw.githubusercontent.com/NyaaCat/nyaautils/maven-repo'
    }
}

dependencies {
    compile('cat.nyaa:nyaautils:2.0-SNAPSHOT') {
        transitive = false
    }
}
```
