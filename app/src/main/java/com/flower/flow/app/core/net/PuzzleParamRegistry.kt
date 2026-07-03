package com.flower.flow.app.core.net

import com.flower.flow.app.core.net.NetUrl.AiArt
import com.flower.flow.app.core.net.NetUrl.Common
import com.flower.flow.app.core.net.NetUrl.Template
import com.flower.flow.app.core.net.NetUrl.User
import com.flower.flow.app.core.util.RandomDataUtil

/**
 * 按 API 路径集中注入混淆用随机参数，避免各 Repository 分散维护。
 */
object PuzzleParamRegistry {

    private data class ParamSpec(val name: String, val randomType: Int)

    private val routeParams: Map<String, List<ParamSpec>> = mapOf(
        Common.LANGUAGE_CONFIG to listOf(
            ParamSpec("annoycoin", 4),
            ParamSpec("alienbollard", 3),
        ),
        Common.LANGUAGE_LIST to listOf(
            ParamSpec("benchbank", 5),
            ParamSpec("differdeceive", 4),
        ),
        Common.WEB_URL to listOf(
            ParamSpec("shapedial", 1),
            ParamSpec("abscissadelta", 3),
        ),
        Common.BACKGROUND_VIDEO to listOf(
            ParamSpec("beetarbiter", 2),
        ),
        Common.SYSTEM_CONFIG to listOf(
            ParamSpec("creepfood", 2),
            ParamSpec("wetdump", 3),
        ),
        Common.ENUM_LIST to listOf(
            ParamSpec("accesscustody", 2),
        ),
        Common.SHARE_INFO to listOf(
            ParamSpec("cadenceamethyst", 4),
            ParamSpec("combbarbecue", 3),
        ),
        Common.ADVICE_ADD to listOf(
            ParamSpec("baldaconite", 3),
            ParamSpec("cringefortune", 1),
        ),
        Common.REPORT_ADD to listOf(
            ParamSpec("gangaccept", 1),
        ),
        User.REGISTER to listOf(
            ParamSpec("backachesmile", 1),
        ),
        User.USER_INFO to listOf(
            ParamSpec("deceitboulevard", 4),
        ),
        User.UPDATE_USER_INFO to listOf(
            ParamSpec("anachronismbutton", 5),
        ),
        User.SWITCH_USER to listOf(
            ParamSpec("dresscraze", 4),
        ),
        User.UPDATE_PASSWORD to listOf(
            ParamSpec("cashanthem", 3),
        ),
        Template.TAG_LIST to listOf(
            ParamSpec("hatebilberry", 3),
            ParamSpec("actinicgift", 5),
        ),
        Template.TOPIC_LIST to listOf(
            ParamSpec("behavemad", 1),
            ParamSpec("anxiousamenable", 4),
        ),
        AiArt.GENERATE_WORK to listOf(
            ParamSpec("acknowledgedevote", 2),
            ParamSpec("crinkleaquarium", 3),
        ),
        AiArt.GENERATE_WORK_AGAIN to listOf(
            ParamSpec("fightbackstage", 5),
        ),
        AiArt.WORK_LIST to listOf(
            ParamSpec("cartel", 5),
        ),
        AiArt.WORK_DELETE to listOf(
            ParamSpec("dazeamiable", 1),
            ParamSpec("abstractought", 1),
        ),
        AiArt.WORK_DOWNLOAD to listOf(
            ParamSpec("drivebride", 3),
        ),
    )

    fun apply(path: String, addParam: (name: String, value: String) -> Unit) {
        routeParams[path]?.forEach { spec ->
            addParam(spec.name, RandomDataUtil.getRandomData(spec.randomType))
        }
    }
}
