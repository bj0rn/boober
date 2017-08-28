package no.skatteetaten.aurora.boober.mapper.v1

import no.skatteetaten.aurora.boober.mapper.AuroraConfigFieldHandler
import no.skatteetaten.aurora.boober.model.*
import no.skatteetaten.aurora.boober.service.openshift.OpenShiftClient

class AuroraConfigMapperV1Development(
        aid: DeployCommand,
        auroraConfig: AuroraConfig,
        openShiftClient: OpenShiftClient,
        vaults: Map<String, AuroraSecretVault>
) : AuroraConfigMapperV1Deploy(aid, auroraConfig, openShiftClient, vaults) {


    //TODO: we cannot use inheritace for Mappers. Because of this.
    override fun extractBuild(): AuroraBuild {
        return AuroraBuild(
                testJenkinsfile = auroraConfigFields.extractOrNull("test/jenkinsfile"),
                testGitUrl = auroraConfigFields.extractOrNull("test/gitUrl"),
                testTag = auroraConfigFields.extractOrNull("test/tag"),
                baseName = auroraConfigFields.extract("baseImage/name"),
                baseVersion = auroraConfigFields.extract("baseImage/version"),
                builderName = auroraConfigFields.extract("builder/name"),
                builderVersion = auroraConfigFields.extract("builder/version"),
                extraTags = auroraConfigFields.extract("extraTags")
        )
    }

    override fun extractTemplate() = null

    val buildHandlers = listOf(
            AuroraConfigFieldHandler("extraTags", defaultValue = "latest,major,minor,patch"),
            AuroraConfigFieldHandler("builder/version", defaultValue = "prod"),
            AuroraConfigFieldHandler("builder/name", defaultValue = "leveransepakkebygger"),
            AuroraConfigFieldHandler("baseImage/name", defaultValue = "oracle8"),
            AuroraConfigFieldHandler("baseImage/version", defaultValue = "1")  ,
            AuroraConfigFieldHandler("test/gitUrl")  ,
            AuroraConfigFieldHandler("test/jenkinsfile")  ,
            AuroraConfigFieldHandler("test/tag")
            )


    override val fieldHandlers = v1Handlers + handlers + buildHandlers


}