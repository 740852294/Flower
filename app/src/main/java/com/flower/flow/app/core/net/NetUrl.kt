package com.flower.flow.app.core.net

import com.flower.flow.BuildConfig
import rxhttp.wrapper.annotation.DefaultDomain

object NetUrl {

    /** 代表请求成功的 code值*/
    const val SUCCESS_CODE = 200

    /** 登录过期code值 */
    const val EXPIRED_CODE = 410

    @DefaultDomain //设置为默认域名
    const val BASE_URL = BuildConfig.BASE_HTTP_API

    object User {
        //注册 /user/addDevice
        const val REGISTER = "flow/aries/zone"
        const val REGISTER_ROUTE = "dentblanch.habit.catalyst.cheddar"

        //获取用户信息 /user/getInfo
        const val USER_INFO = "flow/battle/alphabet"
        const val USER_INFO_ROUTE = "dentblanch.room.agrarian.disdain.collapse.duet"

        //更新用户信息 /user/updateInfo
        const val UPDATE_USER_INFO = "flow/ocean/moon"
        const val UPDATE_USER_INFO_ROUTE = "dentblanch.cinema.upon"

        //切换账号 /user/change
        const val SWITCH_USER = "flow/sure/crack"
        const val SWITCH_USER_ROUTE = "dentblanch.wish.denizen.bacchanal.clad.joke.amatory"

        //修改密码 /user/updatePwd
        const val UPDATE_PASSWORD = "flow/curator/corona"
        const val UPDATE_PASSWORD_ROUTE = "dentblanch.akin.bandstand.baccalaureate.arch"
    }

    object Common {
        //语言文案配置 /sys/getAppLanguageConfig
        const val LANGUAGE_CONFIG = "flow/soon/fee"
        const val LANGUAGE_CONFIG_ROUTE = "dentblanch.craze.garage"

        //语言列表 /language/list
        const val LANGUAGE_LIST = "flow/bank/armpit/bewail"
        const val LANGUAGE_LIST_ROUTE = "dentblanch.labor.under.cricket"

        //网页链接 /sys/getUrl
        const val WEB_URL = "flow/cute/custody"
        const val WEB_URL_ROUTE = "dentblanch.calendar.accomplice.lock.convolve"

        //获取背景视频 /sys/getVideo
        const val BACKGROUND_VIDEO = "flow/demean/align/cache"
        const val BACKGROUND_VIDEO_ROUTE = "dentblanch.dinner.arctic"

        //升级信息 /sys/checkVersion
        const val UPDATE_INFO = "flow/acumen/limit"
        const val UPDATE_INFO_ROUTE = "dentblanch.fatal.blond.button"

        //系统配置 /sys/getGlobal
        const val SYSTEM_CONFIG = "flow/delirium/courage/alimony"
        const val SYSTEM_CONFIG_ROUTE = "dentblanch.avarice.doe.attain.ceramic.empty"

        //枚举列表 /sys/listSysType type：1=意见反馈，2=举报
        const val ENUM_LIST = "flow/aloft/atoll"
        const val ENUM_LIST_ROUTE = "dentblanch.bring.sleep"

        //提交意见反馈 /advice/add
        const val ADVICE_ADD = "flow/stop/raise/arm"
        const val ADVICE_ADD_ROUTE = "dentblanch.askance.clutch.adjourn"

        //提交举报 /report/add
        const val REPORT_ADD = "flow/envelope/butane"
        const val REPORT_ADD_ROUTE = "dentblanch.fancy.east.disc.cabal"

        //系统通知列表 /sys/listSysNotify
        const val SYS_NOTIFY_LIST = "flow/cayenne/anneal"
        const val SYS_NOTIFY_LIST_ROUTE = "dentblanch.consist.ambiguous.memory"

        //分享信息 /sys/getShareInfo
        const val SHARE_INFO = "flow/breathe/accurate/tomorrow"
        const val SHARE_INFO_ROUTE = "dentblanch.exult.shape"
    }

    object Template {
        //模板标签tag列表 /sys/listTag
        const val TAG_LIST = "flow/bounty/aerosol"
        const val TAG_LIST_ROUTE = "dentblanch.accolade.page.carbide.arduous.axe"

        //标签的模板列表 /aiart/pageTagList
        const val TAG_TEMPLATE_LIST = "flow/steam/jaw/avocation"
        const val TAG_TEMPLATE_LIST_ROUTE = "dentblanch.atrophy.brunch.adage"

        //主题列表 /home/getInfo
        const val TOPIC_LIST = "flow/acting/atheist"
        const val TOPIC_LIST_ROUTE = "dentblanch.declare.archangel"

        //主题的模板列表 /aiart/pageList
        const val TOPIC_TEMPLATE_LIST = "flow/decline/chemise"
        const val TOPIC_TEMPLATE_LIST_ROUTE = "dentblanch.letter.arbiter.bier.chaste"
    }

    object AiArt {
        //页面生成作品信息 /aiart/getGenerateSubmitPage
        const val UPLOAD_PAGE_INFO = "flow/cull/adjudge/discord"
        const val UPLOAD_PAGE_INFO_ROUTE = "dentblanch.asterisk.laugh.armor.brake"

        //生成作品 /aiart/generateV1
        const val GENERATE_WORK = "flow/cringe/lunch/acerbic"
        const val GENERATE_WORK_ROUTE = "dentblanch.avast.chip.chop.warm.chenille"

        //再次生成作品 /aiart/againGenerate
        const val GENERATE_WORK_AGAIN = "flow/arrears/despair"
        const val GENERATE_WORK_AGAIN_ROUTE = "dentblanch.chicken.export.adjective.cassette"

        //作品列表 /aiart/pageTaskList
        const val WORK_LIST = "flow/denizen/bohemian"
        const val WORK_LIST_ROUTE = "dentblanch.increase.assassin.brigade.start.fill.devote"

        //删除作品 /aiart/delTask
        const val WORK_DELETE = "flow/describe/compile/grid"
        const val WORK_DELETE_ROUTE = "dentblanch.bump.fountain.faction.bellicose.apathy.edible"

        //下载作品完成 /aiart/downloadTask
        const val WORK_DOWNLOAD = "flow/doe/coddle/abrade"
        const val WORK_DOWNLOAD_ROUTE = "dentblanch.exile.aster.convent.barbel.abnormal"
    }
}
