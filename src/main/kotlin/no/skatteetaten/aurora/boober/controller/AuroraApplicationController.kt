package no.skatteetaten.aurora.boober.controller

import no.skatteetaten.aurora.boober.controller.internal.Response
import no.skatteetaten.aurora.boober.facade.AuroraApplicationFacade
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/aurora/application")
class AuroraApplicationController(val facade: AuroraApplicationFacade) {

    @GetMapping("/namespace/{namespace}/application/{application}")
    fun get(@PathVariable namespace: String, @PathVariable application: String): Response {
        return Response(items = listOf(facade.findApplication(namespace, application)))


    }
}


