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
    }
}