import java.nio.charset.Charset

/**
 * Auto configure.
 */
val artifact = extra["artifact"] as MutableMap<String, Any>

artifact["deploy_version"] = run {
    val GITHUB_REF = System.getenv("GITHUB_REF") ?: ""
    val GITHUB_RUN_NUMBER = System.getenv("GITHUB_RUN_NUMBER")

    // TAG version.
    if (GITHUB_REF.startsWith("refs/tags/v")) {
        return@run GITHUB_REF.substring("refs/tags/v".length)
    }

    val majorMinor = artifact["base_version"] as String
    return@run when {
        GITHUB_RUN_NUMBER?.toIntOrNull() != null -> "$majorMinor.build-$GITHUB_RUN_NUMBER"
        hasProperty("install_snapshot") -> "$majorMinor.99999"
        else -> "$majorMinor-SNAPSHOT"
    }
}.trim()