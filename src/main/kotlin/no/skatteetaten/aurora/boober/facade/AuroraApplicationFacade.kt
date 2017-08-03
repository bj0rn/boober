package no.skatteetaten.aurora.boober.facade

import com.fasterxml.jackson.databind.JsonNode
import no.skatteetaten.aurora.boober.model.AuroraApplication
import no.skatteetaten.aurora.boober.model.AuroraPod
import no.skatteetaten.aurora.boober.service.openshift.OpenShiftResourceClient
import no.skatteetaten.aurora.boober.utils.asMap
import no.skatteetaten.aurora.boober.utils.asOptionalString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class AuroraApplicationFacade(val client: OpenShiftResourceClient, val restTemplate: RestTemplate) {

    val logger: Logger = LoggerFactory.getLogger(AuroraApplicationFacade::class.java)

    fun findApplication(namespace: String, name: String): AuroraApplication? {

        val dc = client.get("deploymentconfig", name, namespace)?.body

        return dc?.let {
            val status = it["status"]

            val metadata = it["metadata"]
            val spec = it["spec"]
            val labels = metadata["labels"]
            val annotations = metadata["annotations"]

            val versionNumber = status["latestVersion"].asOptionalString()?.toInt() ?: 0
            val managementPath = annotations["console.skatteetaten.no/management-path"].asOptionalString()

            val pods = getPods(namespace, managementPath, spec["selector"].asMap().mapValues { it.value.textValue() })
            val phase = getDeploymentPhase(name, namespace, versionNumber)
            val route = getRouteUrls(namespace, name)

            AuroraApplication(
                    name = name,
                    namespace = namespace,
                    affiliation = labels["affiliation"].textValue(),
                    targetReplicas = spec["replicas"].intValue(),
                    availableReplicas = status["availableReplicas"].asOptionalString()?.toInt() ?: 0,
                    deploymentPhase = phase,
                    routeUrl = route,
                    managementPath = managementPath,
                    pods = pods,
                    sprocketDone = annotations["sprocket.sits.no-deployment-config.done"].asOptionalString()
                    //fetch version from prometheus
            )


        }
        //in paralell
        //get status.latestVersion and fetch rc with that name
        //get all pods for aid
        //get route urls
        //get info from prometheus
        //get management endpoints from applications if managementInterface is present

        //if prometheus status reason is HEALTH_CHECK_FAILED fetch health endpoints from pods

    }

    fun getRouteUrls(namespace: String, name: String): String? {
        return client.get("route", name, namespace)?.body?.let {
            getURL(it)
        }
    }

    fun getURL(routeJson: JsonNode): String {

        val spec = routeJson["spec"]
        val scheme = if (spec.has("tls")) "https" else "http"

        val path = if (spec.has("path")) {
            val p = spec["path"].textValue()
            if (!p.startsWith("/")) {
                "/$p"
            } else {
                p
            }
        } else {
            ""
        }

        val host = spec["host"].asText()
        return "$scheme://$host$path"
    }

    fun getPods(namespace: String, managementPath: String?, labels: Map<String, String>): List<AuroraPod> {
        val res = client.list("pod", namespace, labels)

        return res.map {
            val status = it["status"]
            val containerStatus = status["containerStatuses"].get(0)
            val metadata = it["metadata"]
            val labels = metadata["labels"]

            val ip = status["podIP"].asText()

            //we should be able to cache this
            val links: Map<String, String> = managementPath?.let {
                findManagementEndpoints(ip, it)
            } ?: emptyMap()


            val info = findResource(links["info"])
            val health = findResource(links["health"])

            AuroraPod(
                    name = metadata["name"].asText(),
                    status = status["phase"].asText(),
                    restartCount = containerStatus["restartCount"].asInt(),
                    podIP = ip,
                    isReady = containerStatus["ready"].asBoolean(false),
                    deployment = labels["deployment"].asText(),
                    links = links,
                    info = info,
                    health = health,
                    startTime = status["startTime"].asText()
            )
        }
    }

    private fun findResource(url: String?): JsonNode? {
        if (url == null) {
            return null
        }
        try {
            return restTemplate.getForObject(url, JsonNode::class.java)
        } catch (e: RestClientException) {
            return null
        }
    }

    fun getDeploymentPhase(name: String, namespace: String, versionNumber: Int): String? {

        if (versionNumber == 0) {
            return null
        }

        val rc = client.get("replicationcontroller", "$name-$versionNumber", namespace)
        return rc?.body?.at("/metadata/annotations")?.get("openshift.io/deployment.phase")?.asText()

    }

    private fun findManagementEndpoints(podIP: String, managementPath: String): Map<String, String> {

        // val managementUrl = "http://${podIP}$managementPath"
        val managementUrl = "http://localhost:3000/actuator"

        val managementEndpoints = try {
            restTemplate.getForObject(managementUrl, JsonNode::class.java)
        } catch (e: RestClientException) {
            return emptyMap()
        }

        if (!managementEndpoints.has("_links")) {
            logger.warn("Management endpoint does not have links at url={}", managementUrl)
            return emptyMap()
        }
        return managementEndpoints["_links"].asMap().mapValues { it.value["href"].asText() }

    }
}