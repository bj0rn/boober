package no.skatteetaten.aurora.boober.mapper.v1

import no.skatteetaten.aurora.boober.mapper.AuroraConfigFieldHandler
import no.skatteetaten.aurora.boober.mapper.AuroraConfigFields
import no.skatteetaten.aurora.boober.model.AuroraBuild
import no.skatteetaten.aurora.boober.model.TemplateType
import no.skatteetaten.aurora.boober.utils.length
import no.skatteetaten.aurora.boober.utils.notBlank

class AuroraBuildMapperV1 {

    fun build(auroraConfigFields: AuroraConfigFields, dockerRegistry: String): AuroraBuild {

        val type = auroraConfigFields.extract("type", { TemplateType.valueOf(it.textValue()) })
        val name = auroraConfigFields.extract("name")

        val groupId = auroraConfigFields.extract("groupId")
        val artifactId = auroraConfigFields.extract("artifactId")
        val version = auroraConfigFields.extract("version")
        val testGitUrl = auroraConfigFields.extractOrNull("test/gitUrl")

        val skipTriggers = type == TemplateType.development || version.contains("SNAPSHOT") || testGitUrl != null

        val outputKind = if (type == TemplateType.build) {
            "DockerImage"
        } else {
            "ImageStreamTag"
        }

        val outputName = if (type == TemplateType.build) {
            val dockerGroup = groupId.replace(".", "_")
            "$dockerRegistry/$dockerGroup/$artifactId:default"
        } else {
            "$name:latest"
        }

        return AuroraBuild(
                testJenkinsfile = auroraConfigFields.extract("test/jenkinsfile"),
                testGitUrl = testGitUrl,
                testTag = auroraConfigFields.extractOrNull("test/tag"),
                baseName = auroraConfigFields.extract("baseImage/name"),
                baseVersion = auroraConfigFields.extract("baseImage/version"),
                builderName = auroraConfigFields.extract("builder/name"),
                builderVersion = auroraConfigFields.extract("builder/version"),
                extraTags = auroraConfigFields.extract("extraTags"),
                version = version,
                groupId = groupId,
                artifactId = artifactId,
                outputKind = outputKind,
                outputName = outputName,
                triggers = !skipTriggers,
                buildSuffix = auroraConfigFields.extractOrNull("buildSuffix")

        )
    }

    val handlers = listOf(
            AuroraConfigFieldHandler("extraTags", defaultValue = "latest,major,minor,patch"),
            AuroraConfigFieldHandler("buildSuffix"),
            AuroraConfigFieldHandler("builder/version", defaultValue = "prod"),
            AuroraConfigFieldHandler("builder/name", defaultValue = "leveransepakkebygger"),
            AuroraConfigFieldHandler("baseImage/name", defaultValue = "oracle8"),
            AuroraConfigFieldHandler("baseImage/version", defaultValue = "1"),
            AuroraConfigFieldHandler("test/gitUrl"),
            AuroraConfigFieldHandler("test/jenkinsfile", defaultValue = "test.Jenkinsfile"),
            AuroraConfigFieldHandler("test/tag"),
            AuroraConfigFieldHandler("groupId", validator = { it.length(200, "GroupId must be set and be shorter then 200 characters") }),
            AuroraConfigFieldHandler("artifactId", validator = { it.length(50, "ArtifactId must be set and be shorter then 50 characters") }),
            AuroraConfigFieldHandler("version", validator = { it.notBlank("Version must be set") })
    )


}