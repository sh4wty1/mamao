package com.sh4wty.downloader.util

/**
 * Pulls the first http(s) URL out of arbitrary shared text. Apps like TikTok share a blob of
 * promo text with the link embedded, so we can't assume the whole string is the URL.
 */
object UrlExtractor {

    private val URL_REGEX = Regex("""https?://\S+""")

    fun firstUrl(text: String?): String? =
        text?.let { URL_REGEX.find(it)?.value?.trimEnd('.', ',', ')', ']', '"', '\'') }
}
