package no.skatteetaten.aurora.boober.service

import com.fasterxml.jackson.databind.JsonNode
import no.skatteetaten.aurora.boober.Configuration
import no.skatteetaten.aurora.boober.model.ApplicationId
import no.skatteetaten.aurora.boober.model.AuroraConfig
import no.skatteetaten.aurora.boober.model.AuroraConfigFile
import no.skatteetaten.aurora.boober.utils.findAllPointers
import java.io.File

class AuroraConfigHelper

val folder = File(AuroraConfigHelper::class.java.getResource("/samples/config").file)

fun getAuroraConfigSamples(): AuroraConfig {
    val files = folder.walkBottomUp()
            .onEnter { it.name != "secret" }
            .filter { it.isFile }
            .associate { it.relativeTo(folder).path to it }

    val nodes = files.map {
        it.key to convertFileToJsonNode(it.value)
    }.toMap()

    return AuroraConfig(nodes.map { AuroraConfigFile(it.key, it.value!!, false) }, "aos")
}

fun findAllPointers(node: JsonNode, maxLevel: Int) = node.findAllPointers(maxLevel)

@JvmOverloads
fun createAuroraConfig(aid: ApplicationId, affiliation: String = "aos", additionalFile: String? = null): AuroraConfig {
    val files = getSampleFiles(aid, additionalFile)

    return AuroraConfig(files.map { AuroraConfigFile(it.key, it.value!!, false, version = null) }, affiliation)
}

@JvmOverloads
fun getSampleFiles(aid: ApplicationId, additionalFile: String? = null): Map<String, JsonNode?> {

    return collectFilesToMapOfJsonNode(
            "about.json",
            "${aid.application}.json",
            "${aid.environment}/about.json",
            "${aid.environment}/${aid.application}.json",
            additionalFile?.let { it } ?: ""
    )
}

fun getResultFiles(aid: ApplicationId): Map<String, JsonNode?> {
    val baseFolder = File(AuroraConfigHelper::class.java.getResource("/samples/result/${aid.environment}/${aid.application}").file)

    return baseFolder.listFiles().toHashSet().map {
        val json = convertFileToJsonNode(it)
        val name = json?.at("/kind")?.textValue() + "/" + json?.at("/metadata/name")?.textValue()
        name.toLowerCase() to json
    }.toMap()
}

private fun collectFilesToMapOfJsonNode(vararg fileNames: String): Map<String, JsonNode?> {

    return fileNames.filter { !it.isBlank() }.map { it to convertFileToJsonNode(File(folder, it)) }.toMap()
}

private fun convertFileToJsonNode(file: File): JsonNode? {

    val mapper = Configuration().mapper()
    return mapper.readValue(file, JsonNode::class.java)
}

