package no.skatteetaten.aurora.boober.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

import no.skatteetaten.aurora.boober.model.AuroraStatus
import spock.lang.Specification

class AuroraStatusCalculatorTest extends Specification {

  def mapper = new ObjectMapper()

  def "Should get worst status from pods"() {
    given:

      def statuses = [mapper.convertValue(["status": "DOWN"], JsonNode.class),
                      mapper.convertValue(["status": "UP"], JsonNode.class)]

    when:

      def result = AuroraStatusCalculator.findPodStatus(statuses)

    then:

      result == AuroraStatus.AuroraStatusLevel.DOWN

  }

}
