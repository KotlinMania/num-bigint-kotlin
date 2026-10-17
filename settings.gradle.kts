pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "num-bigint-kotlin"

listOf(
    "../num-traits-kotlin",
).forEach { path ->
    if (file(path).exists()) {
        includeBuild(path)
    }
}
