package no.skatteetaten.aurora.boober.facade

import org.springframework.stereotype.Service

@Service
class AuroraApplicationFacade {
    fun findApplication(namespace: String, application: String): Any {


        //hent deploymentConfig
        //hent pods
        //hente route
        //hente info fra prometheus
        //kanskje hente helse status for pods hvis reason i prometheus helse er HELATH_CHECK_FAILED

    }
}