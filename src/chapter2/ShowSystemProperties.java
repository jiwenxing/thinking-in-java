package chapter2;

/**
 * 
 * 打印当前系统属性的属性值列表
 * @author jverson
 *
 */
public class ShowSystemProperties {

	public static void main(String[] args) {
		System.getProperties().list(System.out);
	}
	
}

// 输出如下
/**
 * -- listing properties --
java.runtime.name=Java(TM) SE Runtime Environment
sun.boot.library.path=/Library/Java/JavaVirtualMachines/jdk...
java.vm.version=25.121-b13
user.country.format=CN
gopherProxySet=false
java.vm.vendor=Oracle Corporation
java.vendor.url=http://java.oracle.com/
path.separator=:
java.vm.name=Java HotSpot(TM) 64-Bit Server VM
file.encoding.pkg=sun.io
user.country=US
sun.java.launcher=SUN_STANDARD
sun.os.patch.level=unknown
java.vm.specification.name=Java Virtual Machine Specification
user.dir=/Users/jverson/Study/java/workspace/t...
java.runtime.version=1.8.0_121-b13
java.awt.graphicsenv=sun.awt.CGraphicsEnvironment
java.endorsed.dirs=/Library/Java/JavaVirtualMachines/jdk...
os.arch=x86_64
java.io.tmpdir=/var/folders/d1/cx3tsvsx4b1157cwdv8n5...
line.separator=

java.vm.specification.vendor=Oracle Corporation
os.name=Mac OS X
sun.jnu.encoding=UTF-8
java.library.path=/Users/jverson/Library/Java/Extension...
java.specification.name=Java Platform API Specification
java.class.version=52.0
sun.management.compiler=HotSpot 64-Bit Tiered Compilers
os.version=10.12.4
http.nonProxyHosts=local|*.local|169.254/16|*.169.254/16
user.home=/Users/jverson
user.timezone=
java.awt.printerjob=sun.lwawt.macosx.CPrinterJob
file.encoding=UTF-8
java.specification.version=1.8
user.name=jverson
java.class.path=/Users/jverson/Study/java/workspace/t...
java.vm.specification.version=1.8
sun.arch.data.model=64
java.home=/Library/Java/JavaVirtualMachines/jdk...
sun.java.command=com.jverson.thinking.capter2.ShowSyst...
java.specification.vendor=Oracle Corporation
user.language=en
awt.toolkit=sun.lwawt.macosx.LWCToolkit
java.vm.info=mixed mode
java.version=1.8.0_121
java.ext.dirs=/Users/jverson/Library/Java/Extension...
sun.boot.class.path=/Library/Java/JavaVirtualMachines/jdk...
java.vendor=Oracle Corporation
file.separator=/
java.vendor.url.bug=http://bugreport.sun.com/bugreport/
sun.cpu.endian=little
sun.io.unicode.encoding=UnicodeBig
socksNonProxyHosts=local|*.local|169.254/16|*.169.254/16
ftp.nonProxyHosts=local|*.local|169.254/16|*.169.254/16
sun.cpu.isalist=
 * 
 */