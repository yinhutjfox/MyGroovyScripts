/**
 * 用于根据执行的java（部分）文件名来结束其相对应的进程
 * 基于命令 netstat -ano | find “” 和 taskkill /pid -f
 */

import org.codehaus.groovy.GroovyException

int port = -1

if(0 != args.length) {
    if(args[0].isNumber()) {
        try {
            port = Integer.parseInt(args[0])
        } catch (Exception e) {
            e.printStackTrace()
        }
    } else {
        throw new GroovyException('端口号必须是数字！')
    }
}

if(0 > port || 65535 < port) {
    throw new GroovyException('端口号必须在0~65535之间')
}

def splitLine = {
    String title ->
        println '================================================================================================='
        if(null != title && '' != title) {
            println title
        }
}

static exec(String cmd) {
    if(null == cmd || '' == cmd) {
        throw new GroovyException('请输入执行的命令行参数！')
    }
    return Runtime.getRuntime().exec("cmd.exe /c ${cmd}")
}

def processList = []
def pidSet = new HashSet<String>()
splitLine "正在查找占用端口 ${port} 的进程... ..."
exec("netstat -ano | find \"${port}\"").getInputStream().withReader {
    reader ->
        reader.readLines().each {
            line ->
                processList.add(line.replaceAll('[\t ]+' , ' ').replaceFirst(' ' , '').split(' '))
        }
}
if(!processList.isEmpty()) {
    splitLine '占用端口的进程如下：'
    processList.each {
        println String.join('\t\t' , it)
        pidSet.add(it[4])
    }
    splitLine '正在关闭... ...'
    pidSet.each {
        pid ->
            exec("taskkill /pid ${pid} /f /t").getInputStream().withReader {
                reader ->
                    reader.readLines().each {
                        line ->
                            println line
                    }
            }
    }
} else {
    splitLine '没找到相关进程... ...'
}

splitLine()
