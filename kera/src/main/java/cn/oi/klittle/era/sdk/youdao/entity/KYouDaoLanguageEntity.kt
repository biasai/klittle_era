package cn.oi.klittle.era.sdk.youdao.entity

/**
 * 有道语言实体类
 */
data class KYouDaoLanguageEntity(var sourcePath: String? = null, var outPath: String? = null, var outFileName: String? = null, var from: String? = null, var to: String? = null) {
//        String sourcePath = "D:\\java\\strings\\src\\strings.xml";//源文件
//        String from = "zh-CHS";//从什么语言
//        String to = "en";//翻译到什么语言
//        String outPath = "D:\\java\\strings\\out\\values-" + to;//输入文件路径;不要以\\结尾。
//        String outPath = "D:\\java\\strings\\out\\values-";
//        String outFileName = "strings.xml";

    //复制
    fun copy(): KYouDaoLanguageEntity {
        return copy(sourcePath, outPath, outFileName, from, to)
    }

}