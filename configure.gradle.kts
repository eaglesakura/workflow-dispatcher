import java.nio.charset.Charset

extra["base_version"] = "1.3"
extra["artifact_name"] = project.name
extra["artifact_group"] = "com.eaglesakura.firearm.${extra["artifact_name"]}"
extra["bintray_user"] = "eaglesakura"
extra["bintray_labels"] = arrayOf("android", "kotlin")
extra["bintray_vcs_url"] = "https://github.com/eaglesakura/${project.name}"

/**
 * Auto configure.
 */
extra["artifact_version"] = System.getenv("CIRCLE_TAG").let { CIRCLE_TAG ->
    val majorMinor = if (CIRCLE_TAG.isNullOrEmpty()) {
        rootProject.extra["base_version"] as String
    } else {
        return@let CIRCLE_TAG.substring(CIRCLE_TAG.indexOf('v') + 1)
    }

    val buildNumberFile = rootProject.file(".configs/secrets/build-number.env")
    if (buildNumberFile.isFile) {
        return@let "$majorMinor.build-${buildNumberFile.readText(Charset.forName("UTF-8"))}"
    }

    return@let when {
        hasProperty("install_snapshot") -> "$majorMinor.99999"
        System.getenv("CIRCLE_BUILD_NUM") != null -> "$majorMinor.build-${System.getenv("CIRCLE_BUILD_NUM")}"
        else -> "$majorMinor.snapshot"
    }
}.trim()

