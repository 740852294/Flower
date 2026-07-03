package com.flower.flow.data.repository

import com.flower.flow.app.core.net.FlowHttp
import com.flower.flow.app.core.net.NetUrl
import com.flower.flow.data.model.entity.RegisterResponse
import com.flower.flow.data.model.entity.UserInfo
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.param.toAwaitResponse
import java.io.File

object UserRepository {

    /**
     * 注册 livedata版本
     */
    fun registerLivedata(
        code: String,
        source: Int,
        sourceFlag: String = "",
        step: String = ""
    ): Await<RegisterResponse> {
        return FlowHttp.postForm(NetUrl.User.REGISTER)
            .add("ninesection", code)
            .add("ownathwart", source)
            .add("basilica", sourceFlag)
            .add("corestorm", step)
            .toAwaitResponse()
    }

    /**
     * 获取用户信息
     */
    fun getUserInfoLivedata(): Await<UserInfo> {
        return FlowHttp.get(NetUrl.User.USER_INFO)
            .toAwaitResponse()
    }

    /**
     * 更新用户信息
     *
     * @param name 昵称
     * @param avatarFile 头像图片文件，不传则仅更新昵称
     */
    fun updateUserInfoLivedata(
        name: String,
        avatarFile: File? = null
    ): Await<Any> {
        return FlowHttp.postForm(NetUrl.User.UPDATE_USER_INFO)
            .add("dazzledeacon", name)
            .addFile("file", avatarFile)
            .toAwaitResponse()
    }

    /**
     * 切换账号
     *
     * @param uid 账号 uid（请求头中存储的加密值）
     * @param password 密码明文
     */
    fun switchUserLivedata(
        uid: String,
        password: String
    ): Await<RegisterResponse> {
        return FlowHttp.postForm(NetUrl.User.SWITCH_USER)
            .add("elephantfloat", uid)
            .add("colonybay", password)
            .toAwaitResponse()
    }

    /**
     * 修改密码
     *
     * @param password 新密码，5-15 位数字或字母
     */
    fun updatePasswordLivedata(password: String): Await<String> {
        return FlowHttp.postForm(NetUrl.User.UPDATE_PASSWORD)
            .add("colonybay", password)
            .toAwaitResponse()
    }
}
