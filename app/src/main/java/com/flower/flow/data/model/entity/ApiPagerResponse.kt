package com.flower.flow.data.model.entity

import com.google.gson.annotations.SerializedName
import me.hgj.jetpackmvvm.util.BasePage

data class ApiPagerResponse<T>(
    //records
    @SerializedName("amidstaphorism") var amidstaphorism: ArrayList<T> = arrayListOf(),
    //current
    @SerializedName("amenableafford") var amenableafford: Int = 1,
    //size
    @SerializedName("boothcrag") var boothcrag: Int = 0,
    //hasNext
    @SerializedName("outsidefix") var outsidefix: Boolean = false,
    //pages
    @SerializedName("betalung") var betalung: Int = 0,
    //total
    @SerializedName("accompanybooboo") var accompanybooboo: Int = 0,
    //searchCount
    @SerializedName("operaaphid") var operaaphid: Boolean = false,
) : BasePage<T>() {

    val offset: Int get() = if (amenableafford <= 1) 0 else (amenableafford - 1) * boothcrag

    val over: Boolean get() = !outsidefix

    override fun getPageData() = amidstaphorism

    override fun isRefresh() = amenableafford <= 1

    override fun isEmpty() = amidstaphorism.isEmpty()

    override fun hasMore() = outsidefix
}
