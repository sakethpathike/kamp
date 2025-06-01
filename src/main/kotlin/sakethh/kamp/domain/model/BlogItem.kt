package sakethh.kamp.domain.model

import java.io.File

data class BlogItem(
    val blogName: String, val fileName: String, val description: String, val pubDateTime: String, val file: File
) {
    companion object {
        private val javaClass = object {}.javaClass

        fun getBlogItem(fileName: String): BlogItem {
            val blogFile = javaClass.getResource("/blog/$fileName.md")!!
            val blogMeta = blogFile.readText().substringAfter("---").substringBefore("---").trim()
            val blogName = blogMeta.substringAfter("title:").substringBefore("\n").trim()
            val blogDescription = if (blogMeta.substringAfter("---").substringBefore("---").split("\n")[1].trim()
                    .startsWith("description")
            ) blogMeta.substringAfter("description:").substringBefore("\n").trim() else ""
            val blogPubDateTime = blogMeta.substringAfter("pubDatetime:").substringBefore("\n").trim()

            return BlogItem(
                blogName = blogName,
                fileName = fileName,
                description = blogDescription,
                pubDateTime = blogPubDateTime,
                file = File(blogFile.toURI())
            )
        }
    }
}
