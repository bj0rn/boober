package no.skatteetaten.aurora.boober.mapper.v1

import no.skatteetaten.aurora.boober.mapper.AuroraConfigFieldHandler
import no.skatteetaten.aurora.boober.mapper.AuroraConfigFields
import no.skatteetaten.aurora.boober.model.*
import no.skatteetaten.aurora.boober.service.openshift.OpenShiftClient

class AuroraConfigMapperV1Template(
        aid: DeployCommand,
        auroraConfig: AuroraConfig,
        openShiftClient: OpenShiftClient,
        secretVaultService: Map<String, AuroraSecretVault>
) : AuroraConfigMapperV1(aid, auroraConfig, openShiftClient, secretVaultService) {
    override fun extractDeploy() = null

    override fun extractBuild() = null


    val handlers = listOf(
            AuroraConfigFieldHandler("template", validator = { json ->

                val template = json?.textValue()

                if (template == null) {
                    IllegalArgumentException("Template is required")
                } else if (!openShiftClient.templateExist(template)) {
                    IllegalArgumentException("Template $template does not exist in openshift namespace")
                } else {
                    null
                }
            })
    )

    override val fieldHandlers = v1Handlers + handlers + parameterHandlers

    override fun extractTemplate(): AuroraTemplate? {
        return AuroraTemplate(
                parameters = auroraConfigFields.getParameters(parameterHandlers),
                template = auroraConfigFields.extract("template")
        )
    }

    override fun toAuroraDeploymentConfig(): AuroraDeploymentConfig {

        val type = auroraConfigFields.extract("type", { TemplateType.valueOf(it.textValue()) })

        val name = auroraConfigFields.extract("name")
        return AuroraDeploymentConfigProcessTemplate(
                schemaVersion = auroraConfigFields.extract("schemaVersion"),
                affiliation = auroraConfigFields.extract("affiliation"),
                cluster = auroraConfigFields.extract("cluster"),
                type = type,
                name = name,
                envName = auroraConfigFields.extractOrDefault("envName", deployCommand.applicationId.environment),
                permissions = extractPermissions(),
                secrets = extractSecret(),
                config = auroraConfigFields.getConfigMap(configHandlers),
                template = auroraConfigFields.extract("template"),
                parameters = auroraConfigFields.getParameters(parameterHandlers),
                route = getRoute(name),
                mounts = auroraConfigFields.getMounts(mountHandlers, vaults),
                fields = auroraConfigFields.fields,
                unmappedPointers = getUnmappedPointers(),
                applicationFile = applicationFile.name,
                overrideFiles = overrideFiles
        )
    }
}