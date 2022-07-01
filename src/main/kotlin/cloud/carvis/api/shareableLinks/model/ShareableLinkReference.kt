package cloud.carvis.api.shareableLinks.model

data class ShareableLinkReference(
    val value: String
) {
    override fun toString(): String = value
}

