package cn.oi.klittle.era.entity.feature

//text 搜索的文本
//color 搜索文本的颜色
//isMul true搜索所有匹配字符，false只搜索第一个匹配字符。
data class KSearchEntity(var text: String?, var color: Int, var isMul: Boolean = true) {
}