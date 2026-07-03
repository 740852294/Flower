package com.flower.flow.app.core.net

object ConfuseDataRoute {

    private val routes = mapOf(
        NetUrl.User.REGISTER to NetUrl.User.REGISTER_ROUTE,
        NetUrl.User.USER_INFO to NetUrl.User.USER_INFO_ROUTE,
        NetUrl.User.UPDATE_USER_INFO to NetUrl.User.UPDATE_USER_INFO_ROUTE,
        NetUrl.User.UPDATE_PASSWORD to NetUrl.User.UPDATE_PASSWORD_ROUTE,
        NetUrl.User.SWITCH_USER to NetUrl.User.SWITCH_USER_ROUTE,
        NetUrl.Common.SYSTEM_CONFIG to NetUrl.Common.SYSTEM_CONFIG_ROUTE,
        NetUrl.Common.ENUM_LIST to NetUrl.Common.ENUM_LIST_ROUTE,
        NetUrl.Common.ADVICE_ADD to NetUrl.Common.ADVICE_ADD_ROUTE,
        NetUrl.Common.REPORT_ADD to NetUrl.Common.REPORT_ADD_ROUTE,
        NetUrl.Common.SYS_NOTIFY_LIST to NetUrl.Common.SYS_NOTIFY_LIST_ROUTE,
        NetUrl.Common.SHARE_INFO to NetUrl.Common.SHARE_INFO_ROUTE,
        NetUrl.Common.LANGUAGE_CONFIG to NetUrl.Common.LANGUAGE_CONFIG_ROUTE,
        NetUrl.Common.LANGUAGE_LIST to NetUrl.Common.LANGUAGE_LIST_ROUTE,
        NetUrl.Common.WEB_URL to NetUrl.Common.WEB_URL_ROUTE,
        NetUrl.Common.UPDATE_INFO to NetUrl.Common.UPDATE_INFO_ROUTE,
        NetUrl.Common.BACKGROUND_VIDEO to NetUrl.Common.BACKGROUND_VIDEO_ROUTE,
        NetUrl.Template.TAG_LIST to NetUrl.Template.TAG_LIST_ROUTE,
        NetUrl.Template.TAG_TEMPLATE_LIST to NetUrl.Template.TAG_TEMPLATE_LIST_ROUTE,
        NetUrl.Template.TOPIC_LIST to NetUrl.Template.TOPIC_LIST_ROUTE,
        NetUrl.Template.TOPIC_TEMPLATE_LIST to NetUrl.Template.TOPIC_TEMPLATE_LIST_ROUTE,
        NetUrl.AiArt.WORK_LIST to NetUrl.AiArt.WORK_LIST_ROUTE,
        NetUrl.AiArt.UPLOAD_PAGE_INFO to NetUrl.AiArt.UPLOAD_PAGE_INFO_ROUTE,
        NetUrl.AiArt.GENERATE_WORK to NetUrl.AiArt.GENERATE_WORK_ROUTE,
        NetUrl.AiArt.GENERATE_WORK_AGAIN to NetUrl.AiArt.GENERATE_WORK_AGAIN_ROUTE,
        NetUrl.AiArt.WORK_DELETE to NetUrl.AiArt.WORK_DELETE_ROUTE,
        NetUrl.AiArt.WORK_DOWNLOAD to NetUrl.AiArt.WORK_DOWNLOAD_ROUTE,
    )

    fun routeOf(aliasPath: String): String? = routes[aliasPath.trim('/')]
}
