package no.skatteetaten.aurora.boober.controller.internal

import com.fasterxml.jackson.databind.JsonNode
import no.skatteetaten.aurora.boober.model.ApplicationId
import no.skatteetaten.aurora.boober.model.AuroraConfig
import no.skatteetaten.aurora.boober.model.AuroraConfigFile
import no.skatteetaten.aurora.boober.model.DeployCommand

typealias JsonDataFiles = Map<String, JsonNode>

data class AuroraConfigPayload(
        val files: JsonDataFiles = mapOf(),
        val versions: Map<String, String?> = mapOf()
) {
    fun toAuroraConfig(affiliation: String): AuroraConfig {
        val auroraConfigFiles = files.map { AuroraConfigFile(it.key, it.value, version = versions[it.key]) }
        return AuroraConfig(auroraConfigFiles, affiliation)
    }
}

fun fromAuroraConfig(auroraConfig: AuroraConfig): AuroraConfigPayload {

    val files: JsonDataFiles = auroraConfig.auroraConfigFiles.associate { it.name to it.contents }
    val versions = auroraConfig.auroraConfigFiles.associate { it.name to it.version }
    return AuroraConfigPayload(files, versions = versions)
}

data class SetupParamsPayload(
        val applicationIds: List<ApplicationId> = listOf(),
        val overrides: JsonDataFiles = mapOf()
) {
    fun toSetupParams(): SetupParams {

        val overrideFiles = overrides.map { AuroraConfigFile(it.key, it.value, true) }.toMutableList()
        return SetupParams(applicationIds, overrideFiles)
    }
}

data class SetupCommand(val affiliation: String,
                        val setupParams: SetupParamsPayload
)

data class SetupParams(
        val applicationIds: List<ApplicationId> = listOf(),
        val overrides: MutableList<AuroraConfigFile> = mutableListOf()
) {
    val deployCommands: List<DeployCommand>
        get() = applicationIds.map { aid -> DeployCommand(aid, overrides) }
}
