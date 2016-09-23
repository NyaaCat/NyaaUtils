This patch is used to integrate [InfernalMobs](https://www.spigotmc.org/resources/infernal-mobs.2156/) with the loot protect feature in NyaaUtils.  
See [discussion](https://github.com/NyaaCat/nyaautils/issues/2)

Prepare:

- Download `asm.jar` and `asm-util.jar` from [http://asm.ow2.org/](http://asm.ow2.org/)
- Download `InfernalMobs.jar`
- Have `patch` installed
- Have `java` and `javac` installed

Steps:

1. Generate the bytecode:
       
        java -classpath "InfernalMobs.jar:asm-util.jar:asm.jar" org.objectweb.asm.util.ASMifier io.hotmail.com.jacob_vejvoda.infernal_mobs.EventListener > EventListenerDump.java

2. Apply the patch

        patch EventListenerDump.java < EventListenerDump.patch

3. Compile & generate the class file

        javac -cp asm.jar EventListenerDump.java
        java -cp asm.jar:. EventListenerDump > EventListener.class

4. Copy the patched class back into `InfernalMobs.jar`
