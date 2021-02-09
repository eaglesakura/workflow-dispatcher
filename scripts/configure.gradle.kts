import java.nio.charset.Charset

/**
 * Auto configure.
 */
val GITHUB_REF = System.getenv("GITHUB_REF") ?: ""
val GITHUB_RUN_NUMBER = System.getenv("GITHUB_RUN_NUMBER")
val artifact = extra["artifact"] as MutableMap<String, Any>
val mavencentral = extra["mavencentral"] as MutableMap<String, Any>

artifact["deploy_version"] = run {
    // TAG version.
    if (GITHUB_REF.startsWith("refs/tags/v")) {
        return@run GITHUB_REF.substring("refs/tags/v".length)
    }

    val majorMinor = artifact["base_version"] as String
    return@run when {
        hasProperty("install_snapshot") -> "$majorMinor.99999"
        else -> "${majorMinor}-SNAPSHOT"
    }
}.trim()

mavencentral["repository_url"] = run {
    // TAG version.
    if (GITHUB_REF.startsWith("refs/tags/v")) {
        return@run "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    }

    "https://oss.sonatype.org/content/repositories/snapshots"
}