package com.flower.flow.data.vm

import com.flower.flow.data.model.entity.ApiPagerResponse
import com.flower.flow.data.model.entity.TemplateItem
import com.flower.flow.data.repository.TemplateRepository
import me.hgj.jetpackmvvm.base.vm.BaseViewModel
import me.hgj.jetpackmvvm.core.data.request
import me.hgj.jetpackmvvm.core.net.LoadingType

class TagViewModel : BaseViewModel() {

    private val templatePageCache = mutableMapOf<Int, CachedTagTemplatePage>()
    private var templateCacheGeneration = 0

    fun loadTagList(isLoading: Boolean) = request {
        onRequest {
            TemplateRepository.getTagList().await()
        }
        loadingType = if (isLoading) LoadingType.LOADING_DIALOG else LoadingType.LOADING_NULL
    }

    fun getTemplateCacheGeneration(): Int = templateCacheGeneration

    fun getCachedTemplates(
        tagId: Int,
        generation: Int,
    ): ApiPagerResponse<TemplateItem>? {
        if (generation != templateCacheGeneration) return null
        return templatePageCache[tagId]?.toPage()
    }

    fun cacheTemplates(
        tagId: Int,
        page: ApiPagerResponse<TemplateItem>,
        refresh: Boolean,
        generation: Int,
    ) {
        if (generation != templateCacheGeneration) return
        val items = if (refresh) {
            ArrayList(page.amidstaphorism)
        } else {
            ArrayList(templatePageCache[tagId]?.items.orEmpty()).apply {
                addAll(page.amidstaphorism)
            }
        }
        templatePageCache[tagId] = CachedTagTemplatePage(
            items = items,
            currentPage = page.amenableafford,
            size = page.boothcrag,
            hasNext = page.outsidefix,
            pageCount = page.betalung,
            total = page.accompanybooboo,
            searchCount = page.operaaphid,
        )
    }

    fun invalidateTemplateCache() {
        templateCacheGeneration++
        templatePageCache.clear()
    }

    private data class CachedTagTemplatePage(
        val items: ArrayList<TemplateItem>,
        val currentPage: Int,
        val size: Int,
        val hasNext: Boolean,
        val pageCount: Int,
        val total: Int,
        val searchCount: Boolean,
    ) {
        fun toPage() = ApiPagerResponse(
            amidstaphorism = ArrayList(items),
            amenableafford = currentPage,
            boothcrag = size,
            outsidefix = hasNext,
            betalung = pageCount,
            accompanybooboo = total,
            operaaphid = searchCount,
        )
    }
}
