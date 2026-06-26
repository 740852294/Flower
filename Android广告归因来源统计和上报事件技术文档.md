# 广告归因来源统计和上报事件技术文档


> **重点**：解决 user/addDevice 的 code、source、sourceFlag、step 字段的赋值和上报激活事件、!!#ff0000 上报购买事件（没接支付这个先不做）!!。
> user/addDevice会返回uid（可以认为这一步就是登陆了）

---

## 一、整体方案概述

### 1.1 背景

本项目用原生方案实现广告归因和上报事件：

| 功能 | 使用技术 | 说明 |
|------|----------|------|
| 判断用户来自哪个渠道 | Google Play Install Referrer | Google Play 官方 API，免费，无需第三方 SDK |
| Meta SDK 兜底归因 | Facebook Core SDK（AttributionIdentifiers） | 当 Install Referrer 丢失时，查询 Facebook App 的广告点击缓存 |
| 向 Meta/Facebook 广告后台上报激活事件 | Facebook Core SDK（AppEventsLogger.activateApp） | Meta 官方 SDK，用系统默认方式上报应用激活 |
| 向 Meta/Facebook 广告后台上报购买事件 | Facebook Core SDK（AppEventsLogger.logPurchase） | 用户订阅成功后，上报付费转化事件 |

### 1.2 核心原理

#### 归因判断（双层兜底）

**第一层（优先）：Google Play Install Referrer**

当用户点击 Meta（Facebook/Instagram）广告后跳转到 Google Play 安装 App 时，Google Play 会在安装信息中自动记录一段 **referrer 字符串**，示例如下：

```
utm_source=facebook&utm_medium=cpc&utm_campaign=xxx&fbclid=IwAR3xxx
```

我们读取这段字符串，只要包含以下任意关键词（不区分大小写），判定为 **Meta 广告来源（SOURCE_META = 2）**：

facebook、meta、fb4a、fbclid、pli=1、meta_ads、utm_source=meta_ads、utm_source=facebook、facebook_ads、instagram

**第二层（兜底）：Meta SDK Attribution ID**

当 Install Referrer 参数丢失（商店延迟/跳转异常等），通过 facebook-core 内置的 AttributionIdentifiers 查询 Facebook App 的 ContentProvider。若用户在 28 天内点击过 Meta 广告，Facebook App 会在本地缓存 attributionId，无论 Install Referrer 是否丢失，都能识别该安装为广告量。

**最终判定规则：两层任意一层判定为 Meta 广告，即标记为 SOURCE_META。**

#### 上报逻辑

判断完来源后：
1. 通过 AppEventsLogger.activateApp() 向 Meta 上报**应用激活事件**
2. 将来源值（source）、原始 referrer 字符串（sourceFlag）、注册链路追踪码（step）、设备广告 ID 一起上报到**我们自己的后端**（设备注册接口 user/addDevice）
3. 用户 VIP 订阅成功后，调用 InstallTracker.buildAndReportPurchase() 上报**购买事件**到 Meta 广告后台，再异步调用 googlePlay/updateBackNum 通知服务端已回传金额

---

## 三、依赖配置

### app/build.gradle

在 dependencies 块中，**添加** 以下内容：

```groovy
// Meta App Events SDK（归因兜底 + 激活/购买事件上报到 Meta 广告后台）
implementation 'com.facebook.android:facebook-core:17.0.0'
// Google Play Install Referrer（解析广告来源：第一层归因）
implementation 'com.android.installreferrer:installreferrer:2.2'
// Google Play Services Ads Identifier（读取设备 GAID）
implementation 'com.google.android.gms:play-services-ads-identifier:18.2.0'
```

> **注意**：installreferrer 和 play-services-ads-identifier 原项目已存在，确认版本即可，无需重复添加。

---

## 四、AndroidManifest.xml 配置

### 4.1 权限声明

AD_ID 权限已存在，无需重复添加：

```xml
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
```

### 4.2 Facebook SDK 配置

**写在 application 标签内、所有 activity 之前：**

```xml
<!-- Meta App Events SDK 配置 -->
<meta-data
    android:name="com.facebook.sdk.ApplicationId"
    android:value="@string/fb_app_id" />
<meta-data
    android:name="com.facebook.sdk.ClientToken"
    android:value="@string/facebook_client_token" />
<!-- 禁用自动事件上报：激活事件由归因完成后手动触发，保证合规 -->
<meta-data
    android:name="com.facebook.sdk.AutoLogAppEventsEnabled"
    android:value="false" />
<!--
    移除 Facebook SDK 内置的 FacebookInitProvider，阻止 SDK 在 App 启动时自动初始化。
    AutoInitEnabled=false 在 SDK 17.x 上无法可靠阻止，直接 remove 是唯一可靠方案。
    手动初始化在用户同意隐私协议后由 InstallTracker.boot() 触发。
    【重要】不能删除此段：删掉后 FacebookInitProvider 会在构建合并时重新回到最终 Manifest，
    导致 SDK 在 App 启动时自动初始化，违反隐私合规。
-->
<provider
    android:name="com.facebook.internal.FacebookInitProvider"
    android:authorities="${applicationId}.FacebookInitProvider"
    tools:node="remove" />
```

### 4.3 Manifest 根节点需要 tools 命名空间

确保 manifest 标签声明了 xmlns:tools：

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">
```

---

## 五、strings.xml 配置

在 app/src/main/res/values/strings.xml 中添加：

```xml
<!-- Meta App Events SDK 配置 -->
<string name="fb_app_id">934001xxxxx</string>
<string name="facebook_client_token">1bedd23f0fbb5ae154b6cxxxxxx</string>
```
这配置是错的，你就按错的接，后期我们会换正确的

---

## 六、接口变更说明（user/addDevice）

### 6.1 新增字段总览

| 字段 | 类型 | 是否必传 | 说明 |
|------|------|----------|------|
| code | String | 是 | 设备广告 ID（GAID），关闭广告追踪时为全零 UUID |
| source | Int | 是 | 来源类型：1=自然量，2=Facebook广告量 |
| sourceFlag | String | 否 | Install Referrer 原始字符串，供服务端进一步做精准归因分析 |
| step | String | 否 | 客户端执行代码链路追踪码，供服务端排查 code/source 问题 |

### 6.2 sourceFlag 字段说明

- **内容**：Google Play Install Referrer 返回的原始字符串，例如 utm_source=facebook&utm_medium=cpc&fbclid=xxx
- **用途**：客户端的关键词匹配是二值判断（1或2），原始字符串可供服务端做更精细的分析
- **为空情况**：Install Referrer 服务不可用、断连、异常时，此字段为 null 不传

### 6.3 step 字段说明

step 是由多个片段拼接而成的追踪码，记录从归因到设备 ID 读取的每个关键节点：

**Install Referrer 连接阶段（resolveSource）：**

| step 前缀 | 含义 |
|-----------|------|
| rAS_1 | Install Referrer 成功返回 OK |
| rAS_2 | Install Referrer responseCode 非 OK |
| rAS_3 | Install Referrer 服务断开 |
| rAS_4 | Install Referrer 连接时抛出异常 |

**关键词解析阶段（matchSource）：**

| step 片段 | 含义 |
|-----------|------|
| pSFR_1 | referrer 为空 + Meta SDK 兜底命中（广告量） |
| pSFR_2 | referrer 为空 + Meta SDK 未命中（自然量） |
| pSFR_3 | referrer 关键词命中（广告量，跳过 Meta SDK 查询） |
| pSFR_4 | referrer 关键词未命中 + Meta SDK 兜底命中（广告量） |
| pSFR_5 | referrer 关键词未命中 + Meta SDK 未命中（自然量） |

**Meta SDK Attribution ID 查询结果：**

| step 片段 | 含义 |
|-----------|------|
| iMABFS_0 | 不需要查询（referrer 已命中关键词） |
| iMABFS_1 | Facebook App AttributionId 不为空（命中广告） |
| iMABFS_2 | Facebook App AttributionId 为空（无广告记录） |
| iMABFS_3 | 查询 ContentProvider 异常（Facebook App 未安装等） |

**注册阶段后缀：**

| step 片段 | 含义 |
|-----------|------|
| rDI_7 | 首次注册（boot 流程） |
| rWFC_8 | 重试注册（registerWithFallbackCode 流程） |

**广告 ID 读取结果：**

| step 片段 | 含义 |
|-----------|------|
| rAIWS_1 | 读取成功，返回真实 GAID |
| rAIWS_2 | 读取到的 ID 就是全零 FALLBACK_ID |
| rAIWS_3 | 读取到空字符串，降级为 FALLBACK_ID |
| rAIWS_4 | 读取时抛出异常，降级为 FALLBACK_ID |

**注册失败追踪片段（uid 注册失败时拼接）：**

当设备注册（user/addDevice）未拿到有效 uid 时，会把失败信息拼进 step，供服务端排查到底卡在哪一步：

| step 片段 | 含义 |
|-----------|------|
| _f1Err_xxx | 首次注册请求失败（网络异常/服务端报错），xxx 为净化后的错误信息（仅保留字母数字下划线，最多 20 位） |
| _f1EmptyUid | 首次注册请求 HTTP 成功，但服务端返回的 uid 为空 |
| pF_xxx_ | 上次启动注册双失败进主页时保存的 step，本次启动拼到 step 头部（pF=prevFail，xxx 截断至 60 位） |
| noUid_cntN | 双失败兜底进主页时保存的失败标记，N 为已执行的重试次数 |

> **拼接顺序说明：**
> - `pF_xxx_` 拼在 step **最前面**（boot 启动读取上次失败记录时）
> - `_f1Err_xxx` / `_f1EmptyUid` 在**首次注册结果出来之后**追加，会一并带进 fallback 重试请求（`_rWFC_8`）
> - `noUid_cntN` 在**双失败进主页那一刻**追加，并整体存入 SP，作为下次启动的 `pF_` 来源
> - 注册成功拿到有效 uid 后，会清除 SP 中的历史失败记录，下次启动不再带 `pF_` 前缀

**完整 step 示例：**

```
rAS_1_pSFR_3_iMABFS_0_rDI_7_rAIWS_1
```
含义：Install Referrer 成功 → referrer 关键词命中广告量（无需查 SDK）→ 首次注册 → GAID 读取成功

```
rAS_2_pSFR_1_iMABFS_1_rDI_7_rAIWS_4
```
含义：Install Referrer 非 OK → referrer 为空，Meta SDK 兜底命中 → 首次注册 → GAID 读取异常降级

```
rAS_1_pSFR_5_iMABFS_2_rWFC_8_rAIWS_1
```
含义：Install Referrer 成功 → 关键词未命中且 SDK 无记录（自然量）→ 重试注册 → GAID 读取成功

```
rAS_1_pSFR_5_iMABFS_2_rDI_7_rAIWS_1_f1EmptyUid_rWFC_8_rAIWS_1
```
含义：自然量首次注册成功但返回 uid 为空（_f1EmptyUid）→ 携带该标记重试注册 → GAID 读取成功

```
pF_<上次失败step前60位>_noUid_cnt1_rAS_1_pSFR_5_iMABFS_2_rDI_7_rAIWS_1
```
含义：上次启动注册双失败（重试 1 次仍失败，noUid_cnt1）进主页 → 本次启动把上次 step 以 pF_ 前缀拼到头部 → 重新归因并再次注册

---

## 八、核心代码文件说明

### 8.1 InstallTracker.kt（归因 + 注册 + Meta 上报 统一工具类）

本项目将归因检测、设备注册、Meta 事件上报三个能力**合并在一个 object 中**。

**对外公开的 4 个方法：**

| 方法 | 调用时机 | 说明 |
|------|----------|------|
| boot(context) | 用户同意协议后（由 AppContext.initializeThirdPartySDKs 触发） | 启动整个归因检测 + 注册流程，内部有防重复保护，整个 App 生命周期只执行一次 |
| registerWithFallbackCode(context) | 首次注册失败后重试 | 复用首次归因的 source/sourceFlag/step，不重置来源；suspend 函数，需在协程中调用 |
| ensureDeviceUidBeforeMain() | 进主页跳转（RouteNavigator.go(AppRouter.appMain)）前的最后一道兜底 | 若请求头依赖的 uid 仍为空，再补发一次 user/addDevice；suspend 函数，需在协程中调用，详见 8.1.1 |
| reportPurchase(context, productId, price, currency) | 已有微单位金额和货币时直接调用 | price 为经 backRate 换算后的微单位，内部自动除以 1_000_000 转为主货币后上报 |
| buildAndReportPurchase(context, productId, backRate, productDetailsMap, plans) | VIP 购买验证成功且 backState==1 时 | 从 Google Play 商品详情读取原价，按 backRate 换算后调用 reportPurchase，并返回主货币金额供 updateBackNum 使用 |

#### 8.1.1 进主页前 uid 兜底补注册（ensureDeviceUidBeforeMain）

**背景**：所有网络请求的请求头都会携带 `uid`（取自 `SPNoClearUtil.getUid()`，见 NetworkService 的拦截器）。如果归因/注册流程跑完后 uid 仍为空（注册双失败等异常），用户带着空 uid 进主页，后续所有接口都会缺失身份标识。因此在**真正跳转主页前**（即 `RouteNavigator.go(AppRouter.appMain)` 执行之前）再加一道兜底：判断请求头依赖的 uid 是否为空，为空就再补发一次 `user/addDevice`，拿到 uid 写回后再跳转。

**字段约定（与归因流程不同，这里是纯兜底，不重新归因）：**

| 字段 | 取值 | 说明 |
|------|------|------|
| code | `"00000000-0000-0000-0000-000000000000"` | 固定传全零 UUID（GAID_FALLBACK），不再读取真实 GAID |
| source | `1` | 固定传自然量（SOURCE_ORGANIC） |
| sourceFlag | 之前保存下来的归因来源标识 | 取 `SPNoClearUtil.getDeviceSourceFlag()`，为空则不传 |
| step | `""`（空字符串） | 兜底请求不拼链路追踪码 |

**sourceFlag 的持久化**：原流程未保存 sourceFlag，本次新增——在 `doRegister()` 内、**发起注册请求之前**（无论本次注册成功还是失败）通过 `SPNoClearUtil.saveDeviceSourceFlag(referrerRaw)` 把归因来源标识存到不随登出清除的 SP 区，兜底补注册时再 `getDeviceSourceFlag()` 取回复用。

> **⚠️ 必须在请求前保存，不能放到注册成功分支里！**
> `ensureDeviceUidBeforeMain()` 这道兜底**只在注册失败、uid 仍为空时才触发**。如果把 `saveDeviceSourceFlag()` 放到注册成功之后，那么真正需要它的失败场景下根本没存过值，`getDeviceSourceFlag()` 永远取到空，兜底补注册就丢了来源标识。所以保存动作必须在 `postDeviceAdd` 调用前执行，与注册结果无关。具体位置见下方 8.1 完整代码的 `doRegister()`。

**方法代码（InstallTracker 内）：**

```kotlin
/**
 * 进入主页前的最后一道兜底：若请求头依赖的 uid 仍为空，再补发一次设备注册。
 * code 使用全零降级 ID，source 固定为自然量，sourceFlag 复用历史保存值，step 留空。
 * 注册成功且拿到 uid 时立即写入 SP，保证后续请求头携带有效 uid。
 * suspend 函数，需在协程中调用。
 */
suspend fun ensureDeviceUidBeforeMain() {
    val uid = SPNoClearUtil.getUid()
    if (!uid.isNullOrBlank()) return

    val savedFlag = SPNoClearUtil.getDeviceSourceFlag()
    val result = repo.postDeviceAdd(
        GAID_FALLBACK,                       // code = "00000000-0000-0000-0000-000000000000"
        SOURCE_ORGANIC,                      // source = 1
        savedFlag.takeIf { it.isNotBlank() },// sourceFlag = 之前保存的值
        ""                                   // step = 空
    )
    if (result is ApiResult.Success) {
        val regUid = result.data?.uid.orEmpty()
        if (regUid.isNotBlank()) {
            SPNoClearUtil.saveUid(regUid)
            SPNoClearUtil.clearLastRegFailStep()
        }
        result.data?.id?.let { SPNoClearUtil.saveId(it) }
    }
}
```

**调用方式（navigateToMain，PrivacyPolicyActivity / LaunchActivity 一致）：**

```kotlin
private fun navigateToMain() {
    if (SPNoClearUtil.getUid().isNullOrEmpty()) {
        lifecycleScope.launch {
            try {
                // 挂起等兜底补注册执行完（含网络请求）再跳转
                InstallTracker.ensureDeviceUidBeforeMain()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            RouteNavigator.go(AppRouter.appMain)
            finish()
        }
    } else {
        RouteNavigator.go(AppRouter.appMain)
        finish()
    }
}
```

> **执行顺序**：`ensureDeviceUidBeforeMain()` 是 suspend 挂起函数，协程会先等它整段执行完（包含 `postDeviceAdd` 网络请求 + `saveUid` 写入）才会执行后面的 `RouteNavigator.go(AppRouter.appMain)`，即"兜底补注册走完 → 再进主页"。补注册即使失败/异常也会被 catch 兜住继续进主页，不阻断用户。

> **新增 SP 存储方法（SPNoClearUtil）**：sourceFlag 同样需要存在不随登出清除的持久化区，请补充两个方法：
> - `saveDeviceSourceFlag(sourceFlag: String)`：保存归因来源标识
> - `getDeviceSourceFlag(): String`：读取已保存的来源标识，无则返回空串

**来源判断规则（matchSource，双层）：**

第一层：referrer 字符串包含以下任意关键词（不区分大小写），判定为 Meta 广告量：

| 关键词 | 含义 |
|--------|------|
| facebook | Meta 广告标准 utm_source |
| meta | Meta 品牌关键词 |
| fb4a | Facebook for Android 的渠道标识 |
| fbclid | Facebook Click ID，点击广告时自动附带 |
| pli=1 | Google Play 广告点击标记 |
| meta_ads | Meta 广告渠道标识 |
| utm_source=meta_ads | 完整 UTM 参数 |
| utm_source=facebook | 完整 UTM 参数 |
| facebook_ads | Facebook 广告渠道标识 |
| instagram | Instagram 广告来源 |

第二层（兜底）：referrer 为空或第一层未命中时，查询 Meta SDK AttributionIdentifiers，只要任一层命中即为 SOURCE_META。

**完整代码：**

```kotlin
object InstallTracker {

    private const val SOURCE_ORGANIC = 1
    private const val SOURCE_META = 2
    private const val GAID_FALLBACK = "00000000-0000-0000-0000-000000000000"

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repo by lazy { GeneralRepository() }

    @Volatile private var launched = false
    @Volatile private var registering = false
    @Volatile private var cachedSource = SOURCE_ORGANIC
    @Volatile private var cachedReferrer = ""
    @Volatile private var cachedStep = "1_0_0"

    private data class SourceResult(val source: Int, val referrer: String, val step: String)
    private data class MetaSdkResult(val hit: Boolean, val tag: String)
    private data class GaidResult(val id: String, val stepSuffix: String)

    fun boot(context: Context) {
        if (launched) return
        launched = true
        ioScope.launch {
            val result = resolveSource(context)
            cachedSource = result.source
            cachedReferrer = result.referrer
            cachedStep = result.step

            // 若上次 session 因注册双失败进主页保存了失败 step，拼到本次 step 头部供排查
            val prevFailStep = SPNoClearUtil.getLastRegFailStep()
            if (prevFailStep.isNotEmpty()) {
                val prefix = if (prevFailStep.length > 60) prevFailStep.substring(0, 60) else prevFailStep
                cachedStep = "pF_${prefix}_${cachedStep}"
            }

            triggerActivation(context)
            val uid = SPNoClearUtil.getUid()
            if (!uid.isNullOrBlank()) {
                // 已注册过，清除历史失败记录，直接通知成功
                SPNoClearUtil.clearLastRegFailStep()
                EventBus.getDefault().post(DeviceAddCompleteEvent(true))
                return@launch
            }
            if (registering) return@launch
            registering = true
            val regResult: ApiResult<DeviceBean>? = try {
                doRegister(context, cachedSource, cachedReferrer, "${cachedStep}_rDI_7")
            } finally {
                registering = false
            }
            // 把首次注册失败原因写入 cachedStep，供 fallback 重试请求（_rWFC_8）一并上报
            when (regResult) {
                is ApiResult.Error -> {
                    // message 为你项目 ApiResult.Error 的错误字段，按实际命名调整
                    val errTag = sanitizeForStep(regResult.message)
                    cachedStep = "${cachedStep}_f1Err_${errTag}"
                }
                is ApiResult.Success -> {
                    if (regResult.data?.uid.isNullOrBlank()) {
                        cachedStep = "${cachedStep}_f1EmptyUid"
                    }
                }
                else -> {}
            }
            regResult?.let { handleResult(it) }
        }
    }

    suspend fun registerWithFallbackCode(context: Context) {
        val uid = SPNoClearUtil.getUid()
        if (!uid.isNullOrBlank()) {
            EventBus.getDefault().post(DeviceAddCompleteEvent(true))
            return
        }
        if (registering) {
            EventBus.getDefault().post(DeviceAddCompleteEvent(false))
            return
        }
        registering = true
        // cachedStep 此时已带首次失败标记（_f1Err_xxx / _f1EmptyUid），重试请求会一并携带
        val regResult = try {
            doRegister(context, cachedSource, cachedReferrer, "${cachedStep}_rWFC_8")
        } finally {
            registering = false
        }
        handleResult(regResult)
    }

    fun buildAndReportPurchase(
        context: Context,
        productId: String,
        backRate: Int?,
        productDetailsMap: Map<String, ProductDetails>,
        plans: List<MembershipPlan>
    ): Double? {
        val details = productDetailsMap[productId] ?: return null
        val matchedPlan = plans.find { it.productId == productId }
        val priceAmountMicros: Long
        val currencyCode: String
        if (!matchedPlan?.planId.isNullOrEmpty()) {
            val phase = details.subscriptionOfferDetails
                ?.firstOrNull { it.basePlanId == matchedPlan?.planId }
                ?.pricingPhases?.pricingPhaseList?.firstOrNull() ?: return null
            priceAmountMicros = phase.priceAmountMicros
            currencyCode = phase.priceCurrencyCode
        } else {
            val oneTime = details.oneTimePurchaseOfferDetails ?: return null
            priceAmountMicros = oneTime.priceAmountMicros
            currencyCode = oneTime.priceCurrencyCode
        }
        val scaleFactor = (backRate ?: 100).coerceIn(0, 100) / 100.0
        val adjustedMicros = (priceAmountMicros * scaleFactor).toLong()
        reportPurchase(context, productId, adjustedMicros, currencyCode)
        return adjustedMicros / 1_000_000.0
    }

    // ── 内部方法 ──────────────────────────────────────────────────────────────

    private suspend fun resolveSource(context: Context): SourceResult =
        suspendCancellableCoroutine { cont ->
            try {
                val client = InstallReferrerClient.newBuilder(context).build()
                client.startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(code: Int) {
                        if (code == InstallReferrerClient.InstallReferrerResponse.OK) {
                            val raw = try { client.installReferrer?.installReferrer.orEmpty() } catch (_: Exception) { "" }
                            val (src, step) = matchSource(raw, context, "rAS_1")
                            try { client.endConnection() } catch (_: Exception) {}
                            if (cont.isActive) cont.resume(SourceResult(src, raw, step))
                        } else {
                            try { client.endConnection() } catch (_: Exception) {}
                            val (src, step) = matchSource("", context, "rAS_2")
                            if (cont.isActive) cont.resume(SourceResult(src, "", step))
                        }
                    }
                    override fun onInstallReferrerServiceDisconnected() {
                        val (src, step) = matchSource("", context, "rAS_3")
                        if (cont.isActive) cont.resume(SourceResult(src, "", step))
                    }
                })
            } catch (_: Exception) {
                val (src, step) = matchSource("", context, "rAS_4")
                if (cont.isActive) cont.resume(SourceResult(src, "", step))
            }
        }

    private fun matchSource(referrer: String, context: Context, prefix: String): Pair<Int, String> {
        if (referrer.isBlank()) {
            val r = checkMetaSdk(context)
            return if (r.hit) Pair(SOURCE_META, "${prefix}_pSFR_1_${r.tag}")
            else             Pair(SOURCE_ORGANIC, "${prefix}_pSFR_2_${r.tag}")
        }
        val isMetaRef = referrer.contains("facebook", ignoreCase = true) ||
            referrer.contains("meta", ignoreCase = true) ||
            referrer.contains("fb4a", ignoreCase = true) ||
            referrer.contains("fbclid", ignoreCase = true) ||
            referrer.contains("pli=1", ignoreCase = true) ||
            referrer.contains("meta_ads", ignoreCase = true) ||
            referrer.contains("utm_source=meta_ads", ignoreCase = true) ||
            referrer.contains("utm_source=facebook", ignoreCase = true) ||
            referrer.contains("facebook_ads", ignoreCase = true) ||
            referrer.contains("instagram", ignoreCase = true)
        if (isMetaRef) return Pair(SOURCE_META, "${prefix}_pSFR_3_iMABFS_0")
        val r = checkMetaSdk(context)
        return if (r.hit) Pair(SOURCE_META, "${prefix}_pSFR_4_${r.tag}")
        else             Pair(SOURCE_ORGANIC, "${prefix}_pSFR_5_${r.tag}")
    }

    private fun checkMetaSdk(context: Context): MetaSdkResult {
        return try {
            val ids = AttributionIdentifiers.getAttributionIdentifiers(context)
            if (!ids?.attributionId.isNullOrBlank()) MetaSdkResult(true, "iMABFS_1")
            else MetaSdkResult(false, "iMABFS_2")
        } catch (_: Exception) { MetaSdkResult(false, "iMABFS_3") }
    }

    private suspend fun doRegister(context: Context, source: Int, referrerRaw: String, step: String): ApiResult<DeviceBean> {
        val gaid = readGaid(context)
        // 发起注册请求前先持久化归因来源标识（无论成败），供注册失败后进主页前兜底补注册复用
        if (referrerRaw.isNotBlank()) SPNoClearUtil.saveDeviceSourceFlag(referrerRaw)
        // 不再在此调用 handleResult，改为返回结果，供调用方先拼接失败 step 再统一处理
        return repo.postDeviceAdd(gaid.id, source, referrerRaw.takeIf { it.isNotBlank() }, "${step}_${gaid.stepSuffix}")
    }

    private suspend fun readGaid(context: Context): GaidResult {
        return withContext(Dispatchers.IO) {
            try {
                val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
                val id = info.id.orEmpty()
                when {
                    id == GAID_FALLBACK -> GaidResult(GAID_FALLBACK, "rAIWS_2")
                    id.isNotBlank()     -> GaidResult(id, "rAIWS_1")
                    else                -> GaidResult(GAID_FALLBACK, "rAIWS_3")
                }
            } catch (_: Exception) { GaidResult(GAID_FALLBACK, "rAIWS_4") }
        }
    }

    private fun handleResult(result: ApiResult<DeviceBean>) {
        when (result) {
            is ApiResult.Success -> {
                val uid = result.data?.uid.orEmpty()
                if (uid.isNotBlank()) {
                    SPNoClearUtil.saveUid(uid)
                    SPNoClearUtil.clearLastRegFailStep()   // 注册成功，清除历史失败记录
                }
                result.data?.id?.let { SPNoClearUtil.saveId(it) }
                EventBus.getDefault().post(DeviceAddCompleteEvent(uid.isNotBlank()))
            }
            is ApiResult.Error -> EventBus.getDefault().post(DeviceAddCompleteEvent(false))
        }
    }

    /**
     * 双失败进主页时调用，将当前 step（含失败标记）保存到 SP，
     * 供下次启动 boot() 以 pF_ 前缀拼接追踪。整体最多保留末尾 100 位。
     */
    fun saveFailStepForNextLaunch(reason: String) {
        var stepToSave = "${cachedStep}_${reason}"
        if (stepToSave.length > 100) {
            stepToSave = stepToSave.substring(stepToSave.length - 100)
        }
        SPNoClearUtil.saveLastRegFailStep(stepToSave)
    }

    /**
     * 将错误信息净化为适合拼进 step 的字符串（仅保留字母/数字/下划线，最多 20 位），
     * 避免特殊字符破坏 step 格式或超长。
     */
    private fun sanitizeForStep(raw: String?): String {
        if (raw.isNullOrEmpty()) return "unk"
        val cleaned = raw.replace(Regex("[^a-zA-Z0-9]"), "_")
        return if (cleaned.length > 20) cleaned.substring(0, 20) else cleaned
    }

    private fun triggerActivation(context: Context) {
        val appCtx = context.applicationContext
        @Suppress("DEPRECATION")
        FacebookSdk.sdkInitialize(appCtx) {
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)
            if (BuildConfig.DEBUG) {
                FacebookSdk.setIsDebugEnabled(true)
                FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
            }
            AppEventsLogger.activateApp(AppContext.application)
        }
    }
}
```

> **新增 SP 存储方法（SPNoClearUtil）：** 失败 step 跨 session 追踪需要一个**不随登出/清缓存清除**的持久化键，请在 SPNoClearUtil 中补充三个方法：
> - `getLastRegFailStep(): String`：读取上次保存的失败 step，无则返回空串
> - `saveLastRegFailStep(step: String)`：保存本次失败 step
> - `clearLastRegFailStep()`：注册成功后清除
>
> 另外，示例代码中 `ApiResult.Error` 的错误字段用的是 `message`，请按你项目 ApiResult/网络层的实际命名调整。

---

### 8.2 AppContext.kt（初始化入口）

对外暴露两个入口方法（内容相同，名称不同，兼容两处调用场景）：

```kotlin
// PrivacyPolicyActivity 调用此方法
fun initializeThirdPartySDKs() {
    if (SPNoClearUtil.isAgreeYSAndXY()) {
        InstallTracker.boot(this)
    }
}

// LaunchActivity 调用此方法
fun initializeSDK() {
    initializeThirdPartySDKs()
}
```

> **注意：** 内部检查 isAgreeYSAndXY()，未同意协议时不执行任何操作。不要直接调用 InstallTracker.boot()，统一通过这两个方法。

---

### 8.3 接口层变更

**GeneralAPI.kt（postDeviceAdd 新增两个字段）：**

```kotlin
@FormUrlEncoded
@POST("user/addDevice")
suspend fun postDeviceAdd(
    @Field("code") code: String,
    @Field("source") source: Int,
    @Field("sourceFlag") sourceFlag: String? = null,  // 新增
    @Field("step") step: String? = null               // 新增
): ApiResponse<DeviceBean>
```

## 十、注意事项（重点）

### ⚠️ 10.1 隐私合规——绝对不能在用户同意协议前初始化 SDK

Facebook SDK 和 Install Referrer 的读取**必须**在用户同意隐私协议后才能执行。代码中通过以下两层保护实现：

1. Manifest 中彻底移除 FacebookInitProvider，阻止 SDK 自动启动
2. AppContext.initializeThirdPartySDKs() 内部检查 isAgreeYSAndXY()

**不要**将 InstallTracker.boot() 放到 Application.onCreate() 里直接调用。

---

### ⚠️ 10.2 provider tools:node="remove" 这段绝对不能删

```xml
<provider
    android:name="com.facebook.internal.FacebookInitProvider"
    android:authorities="${applicationId}.FacebookInitProvider"
    tools:node="remove" />
```

这段看起来是「删除一个 provider」，实际上是 **Manifest 合并指令**，告诉构建工具把 Facebook SDK AAR 里自带的 FacebookInitProvider 从最终 Manifest 中移除。一旦删掉这段，FacebookInitProvider 就会重新出现在最终 Manifest 里，SDK 会在 App 进程启动时自动初始化，绕过所有合规保护。

---

### ⚠️ 10.3 boot() 只能执行一次，不要重复调用

InstallTracker.boot() 内部有 if (launched) return 保护，保证整个 App 生命周期内只执行一次归因检测。无需在业务层做去重判断。

---

---

### ⚠️ 10.6 registerWithFallbackCode 必须传 context

重试注册必须调用 InstallTracker.registerWithFallbackCode(applicationContext)，内部会**复用首次归因的 source/sourceFlag/step**，避免重试时来源被错误地设为自然量。

---

### ⚠️ 10.7 设备注册失败不会阻断用户

无论设备注册成功还是失败，最终都会跳转主界面：
1. 第一次失败 → 自动复用归因参数重试一次（registerWithFallbackCode）
2. 重试仍失败 → 直接跳转主界面，不弹报错

---

### ⚠️ 10.8 initializeThirdPartySDKs() 调用后不能立即读 uid

boot() 是异步的（在 IO 协程中执行），调用后函数立即返回，此时后台任务还未完成，uid 一定是空的。等待注册结果必须通过 **EventBus 的 DeviceAddCompleteEvent** 回调实现。

---

### ⚠️ 10.9-1 进主页前必须再判断一次请求头 uid（ensureDeviceUidBeforeMain）

所有接口请求头都携带 `uid`（NetworkService 拦截器取 `SPNoClearUtil.getUid()`）。即便归因/注册流程跑完，仍可能因双失败导致 uid 为空。因此在 `navigateToMain()` 里、`RouteNavigator.go(AppRouter.appMain)` 执行**之前**再判断一次：

1. uid 不为空 → 直接跳转主页。
2. uid 为空 → 在协程里调用 `InstallTracker.ensureDeviceUidBeforeMain()` 再补发一次 `user/addDevice`（`code` 传全零 UUID、`source` 传 1、`sourceFlag` 取之前保存的 `getDeviceSourceFlag()`、`step` 传空），拿到 uid 写回 SP 后再跳转。

注意：

- `ensureDeviceUidBeforeMain()` 是 **suspend 挂起函数**，协程会等它整段执行完（含网络请求 + saveUid 写入）才执行 `RouteNavigator.go(AppRouter.appMain)`，保证"补注册走完 → 再进主页"。
- 补注册失败/异常会被 catch 兜住，仍照常进主页，不阻断用户。
- sourceFlag 的保存是新增逻辑：在 `doRegister()` 里、**发起注册请求之前**（无论成败）通过 `SPNoClearUtil.saveDeviceSourceFlag(referrerRaw)` 持久化，兜底时再取回复用。**切勿放到注册成功分支**，否则注册失败（正是兜底触发的场景）时来源标识没存上，兜底会丢失 sourceFlag。

---

### ⚠️ 10.9 GAID 读取失败时的兜底

如果设备关闭了广告追踪，readGaid() 会返回全零 UUID：00000000-0000-0000-0000-000000000000，step 中会记录具体原因（rAIWS_2/3/4），后端接口需要能正常处理这个值。

---

### ⚠️ 10.10 注册 uid 失败时的 step 拼接

当 user/addDevice 未拿到有效 uid 时，step 会按以下规则追加失败标记，方便服务端定位卡点：

1. **首次注册失败**：请求异常/服务端报错 → `_f1Err_<净化错误>`；HTTP 成功但 uid 为空 → `_f1EmptyUid`。该标记写入 cachedStep 后会一并带进重试请求（`_rWFC_8`）。
2. **双失败兜底**：重试仍失败进主页时，调用 `saveFailStepForNextLaunch("noUid_cntN")` 把当前 step 存入 SP（最多保留末尾 100 位）。
3. **下次启动追踪**：boot() 读取上次失败 step，以 `pF_<前60位>_` 前缀拼到本次 step 头部，从而把"连续多次启动都注册失败"串联起来。
4. **成功即清除**：一旦拿到有效 uid，立即 `clearLastRegFailStep()`，下次启动不再带 pF_ 前缀。

> 错误信息必须经 `sanitizeForStep()` 净化（仅保留字母/数字/下划线，最多 20 位），避免特殊字符破坏 step 格式或超长导致请求异常。失败 step 需存在**不随登出清除**的持久化区（SPNoClearUtil）。

---

## 十一、调试方法

### 11.1 查看 Meta SDK 日志

DEBUG 模式下，triggerActivation() 会开启 Facebook SDK 详细日志：

```kotlin
if (BuildConfig.DEBUG) {
    FacebookSdk.setIsDebugEnabled(true)
    FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
}
```

在 Logcat 中过滤 tag Facebook 可以看到事件发送详情。

### 11.2 通过 step 字段排查归因问题

查看服务端接收到的 step 字段，可以精确定位每次注册的来源路径：

| step 值 | 含义 |
|---------|------|
| rAS_1_pSFR_3_iMABFS_0_rDI_7_rAIWS_1 | 正常流程，Install Referrer 成功 + 关键词命中 + GAID 读取成功 |
| rAS_1_pSFR_5_iMABFS_2_rDI_7_rAIWS_1 | Install Referrer 成功但非 Meta 广告，Meta SDK 也无记录 → 自然量 |
| rAS_2_pSFR_1_iMABFS_1_rDI_7_rAIWS_4 | Install Referrer 失败，Meta SDK 兜底命中，GAID 读取异常 |
| rAS_1_pSFR_3_iMABFS_0_rWFC_8_rAIWS_1 | 首次失败后重试，仍命中广告量，GAID 读取成功 |
| ..._rDI_7_rAIWS_1_f1EmptyUid_rWFC_8_rAIWS_1 | 首次注册 HTTP 成功但返回 uid 为空（_f1EmptyUid），携带标记重试 |
| ..._rDI_7_rAIWS_1_f1Err_timeout_rWFC_8_... | 首次注册请求超时（_f1Err_timeout），携带净化后的错误标记重试 |
| pF_..._noUid_cnt1_rAS_1_... | 上次启动注册双失败（重试 1 次仍失败，noUid_cnt1），本次启动以 pF_ 前缀重新注册 |

开发阶段不推广，只会看到自然量相关的 step 路径（pSFR_2 或 pSFR_5）。

> **排查失败注册：** 看到 step 中含 `_f1Err_`/`_f1EmptyUid` 说明首次注册没拿到 uid；含 `pF_` 前缀说明用户上次启动就注册失败过（连续失败，需重点关注）。