package no.skatteetaten.aurora.boober.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

import no.skatteetaten.aurora.boober.service.AuroraConfigHelperKt
import spock.lang.Specification

class AuroraConfigTest extends Specification {

  def mapper = new ObjectMapper()

  def aid = new ApplicationId("booberdev", "console")

  def "Should get all application ids for AuroraConfig"() {
    given:
      def auroraConfig = AuroraConfigHelperKt.createAuroraConfig(aid)
    when:
      def applicationIds = auroraConfig.getApplicationIds("", "")

    then:
      def console = applicationIds.get(0)
      console.application == "console"
      console.environment == "booberdev"
  }

  def "Should update file"() {
    given:
      def auroraConfig = AuroraConfigHelperKt.createAuroraConfig(aid)
      def updates = mapper.convertValue(["version": "4"], JsonNode.class)

    when:
      def updatedAuroraConfig = auroraConfig.updateFile("booberdev/console.json", updates, "123")

    then:
      def version = updatedAuroraConfig.getAuroraConfigFiles().stream()
          .filter({ it.configName == "booberdev/console.json" })
          .map({ it.contents.get("version").asText() })
          .findFirst()

      version.isPresent()
      "4" == version.get()
  }


  def "Returns files for application"() {
    given:
      def deployCommand = new DeployCommand(aid)
      def auroraConfig = AuroraConfigHelperKt.createAuroraConfig(aid)

    when:
      def filesForApplication = auroraConfig.getFilesForApplication(deployCommand)

    then:
      filesForApplication.size() == 4
  }

  def "Returns files for application with about override"() {
    given:
      def deployCommand = new DeployCommand(aid, [overrideFile("about.json")])
      def auroraConfig = AuroraConfigHelperKt.createAuroraConfig(aid)

    when:
      def filesForApplication = auroraConfig.getFilesForApplication(deployCommand)

    then:
      filesForApplication.size() == 5
  }

  def "Returns files for application with app override"() {
    given:
      def deployCommand = new DeployCommand(aid, [overrideFile("console.json")])
      def auroraConfig = AuroraConfigHelperKt.createAuroraConfig(aid)

    when:
      def filesForApplication = auroraConfig.getFilesForApplication(deployCommand)

    then:
      filesForApplication.size() == 5
  }

  def "Returns files for application with app for env override"() {
    given:

      def deployCommand = new DeployCommand(aid,
          [overrideFile("${aid.environment}/${aid.application}.json")])
      def auroraConfig = AuroraConfigHelperKt.createAuroraConfig(aid)

    when:
      def filesForApplication = auroraConfig.getFilesForApplication(deployCommand)

    then:
      filesForApplication.size() == 5
  }

  def "Fails when some files for application are missing"() {
    given:
      def referanseAid = new ApplicationId("utv", "referanse")
      def files = createMockFiles("about.json", "referanse.json", "utv/about.json")
      def auroraConfig = new AuroraConfig(files, "aos")

    when:
      auroraConfig.getFilesForApplication(new DeployCommand(referanseAid))

    then: "Should be missing utv/referanse.json"
      def ex = thrown(IllegalArgumentException)
      ex.message.contains("utv/referanse.json")
  }

  List<AuroraConfigFile> createMockFiles(String... files) {
    files.collect { new AuroraConfigFile(it, mapper.readValue("{}", JsonNode.class), false, null) }
  }

  def overrideFile(String fileName) {
    new AuroraConfigFile(fileName, mapper.readValue("{}", JsonNode.class), true, null)
  }
}
