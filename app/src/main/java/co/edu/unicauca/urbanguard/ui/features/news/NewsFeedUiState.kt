package co.edu.unicauca.urbanguard.ui.features.news

import co.edu.unicauca.urbanguard.domain.model.News

data class NewsFeedUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val newsList: List<News> = emptyList(),
    val errorMessage: String? = null,
    val isAdmin: Boolean = false,
    val downloadingId: String? = null,
    val downloadError: String? = null,
    val deleteTarget: News? = null,
    val deletingId: String? = null,
    val deleteError: String? = null
)
