package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName
import me.hgj.jetpackmvvm.util.BasePage

data class ApiPagerResponse<T>(
    @SerializedName("records")
    var datas: ArrayList<T> = arrayListOf(),
    @SerializedName("current")
    var curPage: Int = 1,
    @SerializedName("size")
    var size: Int = 0,
    @SerializedName("hasNext")
    var hasNext: Boolean = false,
    @SerializedName("pages")
    var pageCount: Int = 0,
    @SerializedName("total")
    var total: Int = 0,
    @SerializedName("searchCount")
    var searchCount: Boolean = false,
) : BasePage<T>() {

    val offset: Int get() = if (curPage <= 1) 0 else (curPage - 1) * size

    val over: Boolean get() = !hasNext

    override fun getPageData() = datas

    override fun isRefresh() = curPage <= 1

    override fun isEmpty() = datas.isEmpty()

    override fun hasMore() = hasNext
}
