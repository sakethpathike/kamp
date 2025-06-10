package sakethh.kamp.domain.model

data class BlogItem(
    val blogName: String,
    val fileName: String,
    val description: String,
    val pubDateTime: String,
    val rawFileContent: String
) {
    companion object {

        fun getBlogItem(fileName: String): BlogItem {
            val blogFileContent = object {}.javaClass.getResourceAsStream("/blog/$fileName.md")!!.use {
                it.bufferedReader().use {
                    it.readText()
                }
            }

            val blogMeta = blogFileContent.substringAfter("---").substringBefore("---").trim()
            val blogName = blogMeta.substringAfter("title:").substringBefore("\n").trim().replace(oldValue = "#BREAK#", newValue = "")
            val blogDescription = if (blogMeta.substringAfter("---").substringBefore("---").split("\n")[1].trim()
                    .startsWith("description")
            ) blogMeta.substringAfter("description:").substringBefore("\n").trim() else ""
            val blogPubDateTime = blogMeta.substringAfter("pubDatetime:").substringBefore("\n").trim()

            return BlogItem(
                blogName = blogName,
                fileName = fileName,
                description = blogDescription,
                pubDateTime = blogPubDateTime,
                rawFileContent = blogFileContent
            )
        }
    }
}
