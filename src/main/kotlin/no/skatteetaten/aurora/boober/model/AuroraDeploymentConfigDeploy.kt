package no.skatteetaten.aurora.boober.model

import com.fasterxml.jackson.databind.JsonNode
import no.skatteetaten.aurora.boober.mapper.AuroraConfigField

data class AuroraDeploymentConfigDeploy(
        override val schemaVersion: String = "v1",
        override val affiliation: String,
        override val cluster: String,
        override val type: TemplateType,
        override val name: String,
        override val flags: AuroraDeploymentConfigFlags,
        val resources: AuroraDeploymentConfigResources,
        override val envName: String,
        override val permissions: Permissions,
        val replicas: Int?,
        override val secrets: Map<String, String>? = null,
        override val config: Map<String, Map<String, String>>? = null,
        val groupId: String,
        val artifactId: String,
        val version: String,
        val extraTags: String,
        val splunkIndex: String? = null,
        val database: String? = null,
        val certificateCn: String? = null,
        val webseal: Webseal? = null,
        val prometheus: HttpEndpoint? = null,
        val managementPath: String? = null,
        override val fields: Map<String, AuroraConfigField>
) : AuroraDeploymentConfig {

    //In use in velocity template
    val dockerGroup: String = groupId.replace(".", "_")

    //In use in velocity template
    val dockerName: String = artifactId
}

interface AuroraDeploymentConfig {
    val schemaVersion: String
    val affiliation: String
    val cluster: String
    val type: TemplateType
    val name: String
    val envName: String
    val permissions: Permissions
    val secrets: Map<String, String>?
    val config: Map<String, Map<String, String>>?
    val fields: Map<String, AuroraConfigField>
    val flags: AuroraDeploymentConfigFlags
    val namespace: String
        get() = if (envName.isBlank()) affiliation else "$affiliation-$envName"

    //In use in velocity template
    val routeName: String?
        get() = "http://$name-$namespace.$cluster.paas.skead.no"

}

enum class TemplateType {
    deploy, development, localTemplate, template
}


interface AuroraDeploymentConfigProcess {
    val parameters: Map<String, String>?
}


data class AuroraDeploymentConfigProcessLocalTemplate(
        override val schemaVersion: String = "v1",
        override val affiliation: String,
        override val cluster: String,
        override val type: TemplateType,
        override val name: String,
        override val envName: String,
        override val permissions: Permissions,
        override val secrets: Map<String, String>? = null,
        override val config: Map<String, Map<String, String>>? = null,
        override val parameters: Map<String, String>? = mapOf(),
        override val flags: AuroraDeploymentConfigFlags,
        override val fields: Map<String, AuroraConfigField>,
        val templateJson: JsonNode
) : AuroraDeploymentConfigProcess, AuroraDeploymentConfig

data class AuroraDeploymentConfigProcessTemplate(
        override val schemaVersion: String = "v1",
        override val affiliation: String,
        override val cluster: String,
        override val type: TemplateType,
        override val name: String,
        override val envName: String,
        override val permissions: Permissions,
        override val secrets: Map<String, String>? = null,
        override val config: Map<String, Map<String, String>>? = null,
        override val parameters: Map<String, String>? = mapOf(),
        override val flags: AuroraDeploymentConfigFlags,
        override val fields: Map<String, AuroraConfigField>,
        val template: String

) : AuroraDeploymentConfigProcess, AuroraDeploymentConfig


data class AuroraDeploymentConfigFlags(
        val route: Boolean,
        val cert: Boolean = false,
        val debug: Boolean = false,
        val alarm: Boolean = false,
        val rolling: Boolean = false
)

data class AuroraDeploymentConfigResource(
        val min: String,
        val max: String
)

data class AuroraDeploymentConfigResources(
        val memory: AuroraDeploymentConfigResource,
        val cpu: AuroraDeploymentConfigResource
)

data class HttpEndpoint(
        val path: String,
        val port: Int?
)


data class Webseal(
        val path: String,
        val roles: String?
)


data class Permissions(
        val admin: Permission
)

data class Permission(
        val groups: Set<String>?,
        val users: Set<String>?
) {
    val rolebindings: Map<String, String>
        get(): Map<String, String> {
            val userPart = users?.map { Pair(it, "User") }?.toMap() ?: mapOf()
            val groupPart = groups?.map { Pair(it, "Group") }?.toMap() ?: mapOf()
            return userPart + groupPart
        }
}