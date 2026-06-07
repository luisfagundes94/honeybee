pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "honeybee"
include(":app")
include(":core:common")
include(":core:testing")
include(":core:designsystem")
include(":feature:library:impl")
include(":feature:library:api")
include(":feature:onboarding")
include(":feature:onboarding:api")
include(":feature:onboarding:impl")
include(":feature:albums:api")
include(":feature:albums:impl")
include(":feature:config:api")
include(":feature:config:impl")
include(":benchmark")
