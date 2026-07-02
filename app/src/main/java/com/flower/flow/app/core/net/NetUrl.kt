package com.flower.flow.app.core.net

import com.flower.flow.BuildConfig
import rxhttp.wrapper.annotation.DefaultDomain

object NetUrl {

    /** 代表请求成功的 code值， 这里写0 是因为 玩Android 后端成功状态为0 */
    const val SUCCESS_CODE = 200

    /** 登录过期code值 */
    const val EXPIRED_CODE = 410

    @DefaultDomain //设置为默认域名
    const val BASE_URL = BuildConfig.BASE_HTTP_API

    object User {
        const val REGISTER = "user/addDevice"

        //获取用户信息
        const val USER_INFO = "user/getInfo"

        //更新用户信息
        const val UPDATE_USER_INFO = "user/updateInfo"

        //切换账号
        const val SWITCH_USER = "user/change"

        //修改密码
        const val UPDATE_PASSWORD = "user/updatePwd"
    }

    object Common {
        //语言文案配置
        const val LANGUAGE_CONFIG = "sys/getAppLanguageConfig"

        //语言列表
        const val LANGUAGE_LIST = "language/list"

        //网页链接
        const val WEB_URL = "sys/getUrl"

        //获取背景视频
        const val BACKGROUND_VIDEO = "sys/getVideo"

        //升级信息
        const val UPDATE_INFO = "sys/checkVersion"

        //系统配置
        const val SYSTEM_CONFIG = "sys/getGlobal"

        //枚举列表 type：1=意见反馈，2=举报
        const val ENUM_LIST = "sys/listSysType"

        //提交意见反馈
        const val ADVICE_ADD = "advice/add"

        //提交举报
        const val REPORT_ADD = "report/add"

        //系统通知列表
        const val SYS_NOTIFY_LIST = "sys/listSysNotify"

        //分享信息
        const val SHARE_INFO = "sys/getShareInfo"
    }

    object Template {
        //模板标签tag列表
        const val TAG_LIST = "sys/listTag"

        //标签的模板列表
        const val TAG_TEMPLATE_LIST = "aiart/pageTagList"

        //主题列表
        const val TOPIC_LIST = "home/getInfo"

        //主题的模板列表
        const val TOPIC_TEMPLATE_LIST = "aiart/pageList"
    }

    object AiArt {
        //页面生成作品信息
        const val UPLOAD_PAGE_INFO = "aiart/getGenerateSubmitPage"

        //生成作品
        const val GENERATE_WORK = "aiart/generateV1"

        //再次生成作品
        const val GENERATE_WORK_AGAIN = "aiart/againGenerate"

        //作品列表
        const val WORK_LIST = "aiart/pageTaskList"

        //删除作品
        const val WORK_DELETE = "aiart/delTask"

        //下载作品完成
        const val WORK_DOWNLOAD = "aiart/downloadTask"
    }
}
