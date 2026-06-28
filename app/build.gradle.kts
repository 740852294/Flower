import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.BuiltArtifactsLoader
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

fun apkBuildTime(): String {
    return SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
}

abstract class RenameApkTask : DefaultTask() {
    @get:InputDirectory
    abstract val input: DirectoryProperty

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:Internal
    abstract val builtArtifactsLoader: Property<BuiltArtifactsLoader>

    @get:Input
    abstract val outputFileName: Property<String>

    @TaskAction
    fun taskAction() {
        val builtArtifacts = builtArtifactsLoader.get().load(input.get())
            ?: throw GradleException("Cannot load APK artifacts")

        output.get().asFile.mkdirs()
        builtArtifacts.elements.forEach { element ->
            val srcFile = File(element.outputFile)
            val dstFile = output.get().file(outputFileName.get()).asFile
            srcFile.copyTo(dstFile, overwrite = true)
            logger.lifecycle("Copied APK to: ${dstFile.absolutePath}")
        }
    }
}

android {
    namespace = "com.flower.flow"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.flower.flow"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        buildConfigField("String", "VERSION_INT", "\"45\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            buildConfigField("String", "BASE_HTTP_API", "\"http://8.148.151.104:7068/\"")
            buildConfigField("String", "CIPHER_KEY", "\"aikogpap1s2e2288\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "BASE_HTTP_API", "\"http://8.148.151.104:7068/\"")
            buildConfigField("String", "CIPHER_KEY", "\"aikogpap1s2e2288\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        abortOnError = false
    }
}

androidComponents {
    onVariants { variant ->
        val buildType = variant.buildType ?: "unknown"
        val variantTaskName = variant.name.replaceFirstChar { it.titlecase() }
        val versionName = variant.outputs.first().versionName.orNull ?: "unknown"
        val versionCode = variant.outputs.first().versionCode.orNull?.toString() ?: "0"
        val outputFileName = "Flower_${buildType}_${versionName}_${versionCode}_${apkBuildTime()}.apk"

        val renameTask = tasks.register<RenameApkTask>("renameApk$variantTaskName") {
            output.set(layout.buildDirectory.dir("outputs/apk/${variant.name}"))
            builtArtifactsLoader.set(variant.artifacts.getBuiltArtifactsLoader())
            this.outputFileName.set(outputFileName)
        }

        tasks.matching { it.name == "create${variantTaskName}ApkListingFileRedirect" }
            .configureEach { dependsOn(renameTask) }

        variant.artifacts.use(renameTask).wiredWith { it.input }.toListenTo(SingleArtifact.APK)
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":mvvm"))

    implementation(libs.fresco)
    implementation(libs.fresco.vito)
    implementation(libs.fresco.vito.view)
    implementation(libs.fresco.vito.options)
    implementation(libs.fresco.vito.source)
    // 动图 webp
    implementation(libs.fresco.animated.webp)
    // 动图基础支持
    implementation(libs.fresco.animated.base)
    implementation(libs.fresco.webpsupport)

    implementation(libs.facebook.core)
    implementation(libs.install.referrer)
    implementation(libs.play.services.ads.identifier)

    implementation(libs.androidx.activity.ktx)

    //基础库
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.multidex)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.preference.ktx)

    //================================网络框架 sart ===========================//
    implementation(libs.okhttp)
    implementation(libs.rxhttp)
    ksp(libs.rxhttp.compiler)
    //================================网络框架 end ===========================//


    //lottie动画
    implementation(libs.lottie)
}
