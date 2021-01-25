fun convert(file: File) {
    val extensions = setOf(
        "kt",
        "java",
        "xml",
        "md",
        "yaml",
        "yml",
        "json",
        "gradle",
        "kts",
        "gitignore",
        "gitattributes",
        "dic",
        "properties"
    )
    val withoutExtensions = setOf(
        "jar", "class", "bat", "iml"
    )
    when {
        !file.isFile -> return
        file.name.contains("gradlew") -> return
        withoutExtensions.contains(file.extension) -> return
        extensions.contains(file.extension) -> Unit
        else -> return
    }

    val text = file.readText()
    val replaced = if (!text.contains("\r\n")) {
        return
    } else {
        text.replace("\r\n", "\n")
    }

    println("CRLF to LF: ${file.absolutePath}")
    file.writeText(replaced)
}


task("formatLineSeparator") {
    group = "formatting"
    description = "Fix LineSeparator"
    dependsOn("formatKotlin")

    doLast {
        println("Fix line separator in ${project.name}")
        listOfNotNull(
            file(".").listFiles()!!.toList(),
            fileTree("src").files.toList()
        ).flatten()
            .map { it.canonicalFile }
            .forEach { file ->
                convert(file)
            }
    }
}