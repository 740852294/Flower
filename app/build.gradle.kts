import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.artifact.SingleArtifact
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

abstract class RenameApkTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val input: DirectoryProperty

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:Internal
    abstract val transformationRequest: Property<ArtifactTransformationRequest<RenameApkTask>>

    @get:Input
    abstract val buildType: Property<String>

    @get:Input
    abstract val versionName: Property<String>

    @get:Input
    abstract val versionCode: Property<String>

    @TaskAction
    fun taskAction() {
        val buildTime = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val index = AtomicInteger(0)

        transformationRequest.get().submit(this) { artifact ->
            val outputFileName = buildOutputFileName(index.getAndIncrement(), buildTime)
            val dstFile = output.get().file(outputFileName).asFile
            File(artifact.outputFile).copyTo(dstFile, overwrite = true)
            logger.lifecycle("Renamed APK to: ${dstFile.absolutePath}")
            dstFile
        }
    }

    private fun buildOutputFileName(index: Int, buildTime: String): String {
        val suffix = if (index > 0) "_$index" else ""
        return "Flower_${buildType.get()}_${versionName.get()}_${versionCode.get()}${suffix}_$buildTime.apk"
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

        buildConfigField("String", "VERSION_INT", "\"45\"")
    }

    signingConfigs {
        create("test") {
            storeFile = file("key.jks")
            storePassword = "flower"
            keyAlias = "flower"
            keyPassword = "flower"
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("test")
            proguardFiles("src/main/keepRules/rules.keep")
            buildConfigField("String", "BASE_HTTP_API", "\"http://8.148.151.104:7068/\"")
            buildConfigField("String", "CIPHER_KEY", "\"Flow5f6sdedv63er\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("test")
            proguardFiles("src/main/keepRules/rules.keep")
            buildConfigField("String", "BASE_HTTP_API", "\"http://8.148.151.104:7068/\"")
            buildConfigField("String", "CIPHER_KEY", "\"Flow5f6sdedv63er\"")
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

        val renameTaskProvider = tasks.register<RenameApkTask>("renameApk$variantTaskName") {
            this.buildType.set(buildType)
            this.versionName.set(versionName)
            this.versionCode.set(versionCode)
        }

        val apkTransformRequest = variant.artifacts.use(renameTaskProvider)
            .wiredWithDirectories(
                { task -> task.input },
                { task -> task.output },
            )
            .toTransformMany(SingleArtifact.APK)

        renameTaskProvider.configure {
            transformationRequest.set(apkTransformRequest)
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":mvvm"))

    implementation(libs.facebook.core)
    implementation(libs.install.referrer)
    implementation(libs.play.services.ads.identifier)

    implementation(libs.androidx.activity.ktx)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.database)

    //基础库
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.preference.ktx)

    implementation(libs.okhttp)
    implementation(libs.rxhttp)
    ksp(libs.rxhttp.compiler)

    testImplementation(libs.junit)

    implementation(libs.glide)
    ksp(libs.glide.ksp)
    implementation(libs.glide.integration)
    implementation(libs.glide.plugin)
    implementation(libs.glide.awebp)
    implementation(libs.glide.gif)
}
