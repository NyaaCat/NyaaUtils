# nyaautils

Gaming utilities/helpers for NyaaCat Minecraft Server

Detailed function manual please refer to [Wiki](https://github.com/NyaaCat/nyaautils/wiki).

# Use in Gradle

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
