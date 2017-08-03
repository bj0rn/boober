package no.skatteetaten.aurora.boober.model

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

data class AuroraApplication(
        val name: String,
        val namespace: String,
        val affiliation: String? = null,
        val version: Version? = null,
        val sprocketDone: String? = null,
        val targetReplicas: Int = 0,
        val availableReplicas: Int = 0,
        val managementPath: String? = null,
        val deploymentPhase: String? = null,
        val routeUrl: String? = null,
        val pods: List<AuroraPod>? = null
        //val status: AuroraStatus? = null
)

//finne fra prometheus
data class Version(
        val tagCreated: Instant,
        val auroraVersion: AuroraVersion,
        val runtimeType: String,
        val runtimeVersion: String,
        val deployTag: String)

//finne fra prometheus
data class AuroraVersion(
        val auroraVersion: String,
        val applicationVersion: String,
        val builderVersion: String,
        val baseImageVersion: String,
        val baseImageName: String)

data class AuroraPod(
        val name: String,
        val status: String,
        //val appStatus: AuroraStatus, //prometheus
        val restartCount: Int = 0,
        val podIP: String,
        val isReady: Boolean = false,
        val startTime: String,
        val deployment: String,
        val info: JsonNode?,
        val health: JsonNode? = null,
        val links: Map<String, String>? = null //cache
)


