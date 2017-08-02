package no.skatteetaten.aurora.boober.model

import java.time.Instant

data class AuroraApplication(
        val id: ApplicationId? = null,
        val affiliation: String? = null,
        val version: Version,
        val configMap: String? = null,
        val sprocketDone: String? = null,
        val targetReplicas: Int = 0,
        val availableReplicas: Int = 0,
        val managementPath: String? = null,
        val deploymentPhase: String? = null,
        val routeUrls: List<String>? = null,
        val pods: List<AuroraPod>? = null,
        val status: AuroraStatus? = null
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
        val selfLink: String,
        val deployment: String,
        val links: Map<String, String>? = null //cache
)


data class AuroraStatus(

        val level: AuroraStatusLevel? = null,
        val comment: String? = null) {

    enum class AuroraStatusLevel {
        DOWN,
        OFF,
        UNKNOWN,
        OBSERVE,
        HEALTHY
    }

    companion object {

        val AVERAGE_RESTART_OBSERVE_THRESHOLD = 20
        val AVERAGE_RESTART_ERROR_THRESHOLD = 100
        val DIFFERENT_DEPLOYMENT_HOUR_THRESHOLD = 2

        fun fromApplicationStatus(status: String, message: String): AuroraStatus {

            var level = AuroraStatusLevel.UNKNOWN

            if ("UP".equals(status, ignoreCase = true)) {
                level = AuroraStatusLevel.HEALTHY
            }

            if ("OBSERVE".equals(status, ignoreCase = true)) {
                level = AuroraStatusLevel.OBSERVE
            }

            if ("UNKNOWN".equals(status, ignoreCase = true)) {
                level = AuroraStatusLevel.UNKNOWN
            }

            if ("OUT_OF_SERVICE".equals(status, ignoreCase = true)) {
                level = AuroraStatusLevel.DOWN
            }

            if ("DOWN".equals(status, ignoreCase = true)) {
                level = AuroraStatusLevel.DOWN
            }

            return AuroraStatus(level, message)
        }
    }
}
