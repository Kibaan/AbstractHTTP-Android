package kibaan.android.abstracthttp.entity


data class Post(
    val userId: Int,
    val id: Int,
    val title: String?,
    val body: String?
) {
    val stringValue: String
        get() = """
            ID: ${id}
            UserID: ${userId}
            Title: ${title ?: ""}
            Body: ${body ?: ""}
        """
}
