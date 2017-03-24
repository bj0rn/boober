package no.skatteetaten.aurora.boober

import no.skatteetaten.aurora.boober.model.*


class ValidationHelper {

    val process = ProcessConfig(
            name = "foo",
            cluster = "utv",
            affiliation = "aos")

    val config = AppConfig(
            cluster = "utv",
            affiliation = "aos",
            build = ConfigBuild("foo", "bar", "1.0.0"),
            type = TemplateType.deploy)

    fun validMinimalConfig(): Result {

        return Result(
                config = config)
    }

    fun configWithNameAndBuildArtifactIdMissing(): Result {
        val buildWithMissingArtifactId = ConfigBuild("", "foo", "1.0.0")
        return Result(config = config.copy(build = buildWithMissingArtifactId))
    }

    fun affiliationWithSpecialChar(): Result = Result(config = config.copy(affiliation = "L337"))

    fun processWithTemplateFile(templateFile: String) = Result(
            config = process.copy(templateFile = templateFile))

    fun processWithTemplate(template: String) = Result(config = process.copy(template = template))


    fun processWithTemplateAndTemplateFile(template: String, file: String) =
            Result(config = process.copy(template = template, templateFile = file))
}