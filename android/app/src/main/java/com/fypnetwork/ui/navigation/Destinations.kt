package com.fypnetwork.ui.navigation

object Destinations {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FEED = "feed"
    const val CREATE_POST = "create_post"
    const val POST_DETAIL = "post_detail/{postId}"
    const val GROUPS = "groups"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val CONNECTIONS = "connections"
    const val USER_PROFILE = "user_profile/{userId}"

    fun groupDetail(groupId: String) = "group_detail/$groupId"
    fun postDetail(postId: String) = "post_detail/$postId"
    fun userProfile(userId: String) = "user_profile/$userId"

    // Bottom-nav tabs, in display order.
    val bottomNavTabs = listOf(FEED, GROUPS, NOTIFICATIONS, PROFILE)
}
