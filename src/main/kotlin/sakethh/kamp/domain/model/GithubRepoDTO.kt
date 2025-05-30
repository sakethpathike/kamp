package sakethh.kamp.domain.model

data class GithubRepoDTO(
    val name: String,
    val description: String,
    val starCount: String,
    val programmingLanguage: String,
    val tags: List<String>
)
