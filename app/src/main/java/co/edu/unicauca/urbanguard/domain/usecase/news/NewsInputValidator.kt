package co.edu.unicauca.urbanguard.domain.usecase.news

import co.edu.unicauca.urbanguard.core.common.NewsConstants

internal object NewsInputValidator {

    fun validate(title: String, body: String): String? {
        val trimmedTitle = title.trim()
        val trimmedBody = body.trim()
        if (trimmedTitle.isEmpty()) return NewsConstants.MSG_TITLE_REQUIRED
        if (trimmedTitle.length > NewsConstants.TITLE_MAX_LENGTH) {
            return NewsConstants.MSG_TITLE_TOO_LONG
        }
        if (trimmedBody.isEmpty()) return NewsConstants.MSG_BODY_REQUIRED
        if (trimmedBody.length > NewsConstants.BODY_MAX_LENGTH) {
            return NewsConstants.MSG_BODY_TOO_LONG
        }
        if (containsControlCharacters(trimmedTitle) || containsControlCharacters(trimmedBody)) {
            return NewsConstants.MSG_INVALID_CHARS
        }
        return null
    }

    private fun containsControlCharacters(text: String): Boolean {
        return text.any { char ->
            Character.isISOControl(char)
        }
    }
}
