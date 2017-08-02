package no.skatteetaten.aurora.boober.service.exception

import no.skatteetaten.aurora.boober.service.internal.ServiceException

class DockerServiceErrorException(message: String, cause: Throwable) : ServiceException(message, cause)