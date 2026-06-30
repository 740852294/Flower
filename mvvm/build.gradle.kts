plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "me.hgj.jetpackmvvm"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
    }

    kotlin {
        jvmToolchain(17) // 自动设置 Java 17 兼容性
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.flexbox)

    // 快速构建RV列表工具
    api(libs.brv)
    // 协程基础库
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)
    //lifecycle
    api(libs.androidx.lifecycle.runtime.ktx)
    // viewModel
    api(libs.androidx.lifecycle.viewmodel.ktx)
    // liveData
    api(libs.androidx.lifecycle.livedata.ktx)

    //Toast，使用可在任意地方 message.toast()
    api(libs.toaster)
    //json数据解析，框架封装可看 GsonExt 类
    api(libs.gson)
    api(libs.dialog)
    api(libs.dialog.lifecycle)
    api(libs.immersionbar)
    //缓存框架，框架封装 在 Cache 类 中
    api(libs.mmkv)
    //http
    implementation(libs.okhttp)
    //refresh刷新
    api(libs.refresh.layout.kernel)
    api(libs.refresh.header.material)
    api(libs.refresh.footer.ball)
    //利用liveData发送全局消息
    api(libs.unpeek.livedata)
    //通过标签直接生成shape，无需再写shape.xml
    api(libs.shape.view)
    api(libs.shape.drawable)
    //权限申请
    api(libs.permissions)
}