package sakethh.kamp.domain.model

data class MetaTags(
    val ogImageSrc: String, val ogTitle: String, val ogDescription: String, val pageType: PageType, val deployTarget: DeployTarget
) {
    companion object {
        fun HomePage(deployTarget: DeployTarget): MetaTags {
            return MetaTags(
                ogImageSrc = "/images/ogImage-home.png",
                ogTitle = "Saketh Pathike | Android & Kotlin Multiplatform Developer",
                ogDescription = "",
                deployTarget = deployTarget,
                pageType = PageType.Website
            )
        }

        fun BlogListPage(deployTarget: DeployTarget): MetaTags {
            return MetaTags(
                ogImageSrc = "/images/ogImage-blogList.png",
                ogTitle = "Blogs ${Typography.bullet} Saketh Pathike",
                ogDescription = "",
                pageType = PageType.Website,
                deployTarget = deployTarget
            )
        }

        fun BlogPage(fileName: String, deployTarget: DeployTarget): MetaTags {
            return BlogItem.getBlogItem(fileName).run {
                MetaTags(
                    ogImageSrc = "/images/ogImage-${fileName}.png",
                    ogTitle = "${this.blogName} ${Typography.bullet} Saketh Pathike",
                    ogDescription = "",
                    pageType = PageType.Article,
                    deployTarget = deployTarget
                )
            }
        }
    }
}

enum class PageType(val type: String) {
    Article("article"), Website("website")
}

enum class DeployTarget(val baseUrl: String) {
    Koyeb(baseUrl = "https://energetic-tina-sakethpathike-52a4b526.koyeb.app"), GithubPages(baseUrl = "https://sakethpathike.github.io")
}