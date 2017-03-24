package no.skatteetaten.aurora.boober.service

import no.skatteetaten.aurora.boober.model.ProcessConfig
import no.skatteetaten.aurora.boober.model.Result
import org.springframework.stereotype.Service
import javax.validation.Validation


@Service
class ValidationService(val openshiftService: OpenshiftService) {

    fun validate(res: Result, token: String): Result {

        val config = res.config ?: return res

        val validator = Validation.buildDefaultValidatorFactory().validator
        val err = validator.validate(config)

        val errors: MutableList<String> = mutableListOf()

        if (err.isNotEmpty()) {
            val map = err.map { "${it.message} for field ${it.propertyPath}" }
            errors.addAll(map)
        }

        if (config is ProcessConfig) {

            if (config.templateFile != null && !res.sources.keys.contains(config.templateFile)) {
                errors.add("Template file ${config.templateFile} is missing in sources")
            }

            if(config.template !=null && config.templateFile != null) {
                errors.add("Cannot specify both template and templateFile")

            }

            if (config.template != null && !openshiftService.templateExist(token, config.template)) {
                errors.add("Template ${config.template} does not exist in cluster.")
            }

        }

        return res.copy(errors = res.errors.plus(errors))
    }
}
