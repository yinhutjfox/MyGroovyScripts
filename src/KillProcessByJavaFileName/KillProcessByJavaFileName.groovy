/**
 * 用于根据执行的java（部分）文件名来结束其相对应的进程
 * 基于命令 jps -l | find “” 和 taskkill /pid -f
 */

import org.codehaus.groovy.GroovyException

def fileName = 'com-boot-test'
if (0 != args.length) {
    fileName = args[0]
}
if (null == fileName || '' == fileName) {
    throw new GroovyException('请输入完整文件名或一部分')
}

def splitLine = {
    String title ->
        println '================================================================================================='
        if (null != title && '' != title) {
            println title
        }
}

static exec(String cmd) {
    if (null == cmd || '' == cmd) {
        throw new GroovyException('请输入执行的命令行参数！')
    }
    return Runtime.getRuntime().exec("cmd.exe /c ${cmd}")
}

List<String> processList = []
splitLine "正在查找文件名与 ${fileName} 相关的进程... ..."
exec("jps -l | find \"${fileName}\"").getInputStream().withReader {
    processList = it.readLines().findAll {
        !it.containsIgnoreCase('jenkins') && !it.containsIgnoreCase('sun.tools.jps.Jps')
    }
}
if (!processList.isEmpty()) {
    splitLine '找到以下相关进程'
    processList.each {
        println it
    }
    splitLine '正在关闭... ...'
    processList.each {
        String processLine ->
            exec("taskkill /pid ${processLine.split(' ')[0]} /f /t")
                    .getInputStream().withReader {
                reader ->
                    println reader.readLines()
            }
    }
} else {
    splitLine '没有找到相关进程！'
}

splitLine()
