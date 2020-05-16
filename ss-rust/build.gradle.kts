import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    id("org.mozilla.rust-android-gradle.rust-android")
}

android {

    ndkVersion = "21.1.6352462"

    compileSdkVersion(29)
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(28)
    }

}

cargo {
    module = "src/main/rust/shadowsocks-rust"
    libname = "ss-local"
    targets = listOf("arm", "arm64", "x86", "x86_64")
    profile = findProperty("CARGO_PROFILE")?.toString() ?: "release"
    extraCargoBuildArguments = listOf("--bin", "sslocal")
    featureSpec.noDefaultBut(arrayOf(
            "sodium",
            "rc4",
            "aes-cfb",
            "aes-ctr",
            "camellia-cfb",
            "openssl-vendored"))
    exec = { spec, toolchain ->
        spec.environment("RUST_ANDROID_GRADLE_LINKER_WRAPPER_PY", "$projectDir/$module/../linker-wrapper.py")
        spec.environment("RUST_ANDROID_GRADLE_TARGET", "target/${toolchain.target}/$profile/lib$libname.so")
    }
}

tasks.whenTaskAdded {
    when (name) {
        "mergeDebugJniLibFolders", "mergeReleaseJniLibFolders" -> dependsOn("cargoBuild")
    }
}

tasks.register<Exec>("cargoClean") {
    executable("cargo")     // cargo.cargoCommand
    args("clean")
    workingDir("$projectDir/${cargo.module}")
}

tasks.clean.dependsOn("cargoClean")