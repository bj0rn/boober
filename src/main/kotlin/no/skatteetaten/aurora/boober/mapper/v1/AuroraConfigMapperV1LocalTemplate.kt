package no.skatteetaten.aurora.boober.mapper.v1

import com.fasterxml.jackson.databind.JsonNode
import no.skatteetaten.aurora.boober.mapper.AuroraConfigFieldHandler
import no.skatteetaten.aurora.boober.mapper.AuroraConfigFields
import no.skatteetaten.aurora.boober.mapper.findConfig
import no.skatteetaten.aurora.boober.mapper.findParameters
import no.skatteetaten.aurora.boober.model.*
import no.skatteetaten.aurora.boober.service.openshift.OpenShiftClient

class AuroraConfigMapperV1LocalTemplate(aid: ApplicationId,
                                        auroraConfig: AuroraConfig,
                                        allFiles: List<AuroraConfigFile>,
                                        openShiftClient: OpenShiftClient) :
        AuroraConfigMapperV1(aid, auroraConfig, allFiles, openShiftClient) {

    val handlers = listOf(
            AuroraConfigFieldHandler("templateFile", validator = { json ->
                val fileName = json?.textValue()
                if (auroraConfig.auroraConfigFiles.none { it.name == fileName }) {
                    IllegalArgumentException("The file named $fileName does not exist in AuroraConfig")
                } else {
                    null
                }
            })
    )

    override val fieldHandlers = v1Handlers + handlers + allFiles.findParameters()
    override val auroraConfigFields = AuroraConfigFields.create(fieldHandlers, allFiles)

    override fun toAuroraDeploymentConfig(): AuroraDeploymentConfig {

        val type = auroraConfigFields.extract("type", { TemplateType.valueOf(it.textValue()) })

        val templateJson = extractTemplateJson()

        return AuroraDeploymentConfigProcessLocalTemplate(
                schemaVersion = auroraConfigFields.extract("schemaVersion"),
                affiliation = auroraConfigFields.extract("affiliation"),
                cluster = auroraConfigFields.extract("cluster"),
                type = type,
                name = auroraConfigFields.extract("name"),
                envName = auroraConfigFields.extractOrDefault("envName", aid.environmentName),
                permissions = extractPermissions(),
                secrets = extractSecret(),
                config = auroraConfigFields.getConfigMap(allFiles.findConfig()),
                templateJson = templateJson,
                parameters = auroraConfigFields.getParameters(allFiles.findParameters()),
                flags = AuroraDeploymentConfigFlags(
                        auroraConfigFields.extract("flags/route", { it.asText() == "true" })
                ),
                fields = auroraConfigFields.fields
        )

    }

    private fun extractTemplateJson(): JsonNode {
        val templateFile = auroraConfigFields.extract("templateFile").let { fileName ->
            auroraConfig.auroraConfigFiles.find { it.name == fileName }?.contents
        }
        return templateFile ?: throw IllegalArgumentException("templateFile is required")
    }


}