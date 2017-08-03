package no.skatteetaten.aurora.boober.controller

import no.skatteetaten.aurora.boober.controller.internal.Response
import no.skatteetaten.aurora.boober.facade.AuroraApplicationFacade
import no.skatteetaten.aurora.boober.model.AuroraApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/aurora/application")
class AuroraApplicationController(val facade: AuroraApplicationFacade) {

    @GetMapping("/namespace/{namespace}/application/{application}")
    fun get(@PathVariable namespace: String, @PathVariable application: String): Response {
        val result = facade.findApplication(namespace, application)

        val lst: List<AuroraApplication> = result?.let { listOf(it) } ?: emptyList()

        return Response(items = lst)


    }
}


