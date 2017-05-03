package no.skatteetaten.aurora.boober.service

import static no.skatteetaten.aurora.boober.utils.SampleFilesCollector.getQaEbsUsersSampleFiles
import static no.skatteetaten.aurora.boober.utils.SampleFilesCollector.jsonToMap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import no.skatteetaten.aurora.boober.controller.security.UserDetailsProvider
import no.skatteetaten.aurora.boober.model.AuroraConfig
import no.skatteetaten.aurora.boober.model.AuroraConfigFile
import no.skatteetaten.aurora.boober.model.AuroraDeploymentConfig
import spock.lang.Specification
import spock.mock.DetachedMockFactory

@SpringBootTest(classes = [
    no.skatteetaten.aurora.boober.Configuration,
    UserDetailsProvider,
    AuroraConfigService,
    GitService,
    OpenShiftClient,
    EncryptionConfig,
    EncryptionService,
    OpenshiftResourceClient
])
class AuroraConfigServiceTest extends Specification {

  public static final String ENV_NAME = "booberdev"
  public static final String APP_NAME = "verify-ebs-users"
  final ApplicationId aid = new ApplicationId(ENV_NAME, APP_NAME)

  @Configuration
  static class Config {
    private DetachedMockFactory factory = new DetachedMockFactory()

    @Bean
    OpenShiftClient openShiftClient() {
      factory.Mock(OpenShiftClient)
    }

    @Bean
    OpenshiftResourceClient openshiftResourceClient() {
      factory.Mock(OpenshiftResourceClient)
    }

    @Bean
    UserDetailsProvider userDetailsProvider() {

      factory.Stub(UserDetailsProvider)
    }

    @Bean
    GitService gitService() {
      factory.Mock(GitService)
    }
  }

  @Autowired
  UserDetailsProvider userDetailsProvider

  @Autowired
  AuroraConfigService service

  private static AuroraConfig createAuroraConfig(Map<String, Map<String, Object>> files, Map<String, String> secrets = [:]) {
    new AuroraConfig(files.collect { new AuroraConfigFile(it.key, it.value, false) }, secrets)
  }

  def "Should create an AuroraConfigFields"() {
    given:
      Map<String, Map<String, Object>> files = getQaEbsUsersSampleFiles()
      files.put("booberdev/about.json", ["type": "deploy", "cluster": "utv"])
      AuroraConfig auroraConfig = createAuroraConfig(files)

    when:
      def fields = service.mergeFiles(aid, [], auroraConfig)

    then:
      fields.size() == 1
  }

  def "Should create an AuroraConfigFields with overrides"() {
    given:
      def overrides = [new AuroraConfigFile("about.json", ["type": "foobar"], true)]
      Map<String, Map<String, Object>> files = getQaEbsUsersSampleFiles()
      files.put("booberdev/about.json", ["type": "deploy", "cluster": "utv"])
      AuroraConfig auroraConfig = createAuroraConfig(files)

    when:
      def fields = service.mergeFiles(aid, overrides, auroraConfig)

    then:
      fields.size() == 1
  }

  /* TODO:fix
  def "Should create an AuroraDeploymentConfig with default tag when type is deploy"() {
    given:
      Map<String, Map<String, Object>> files = getQaEbsUsersSampleFiles()
      files.put("booberdev/about.json", ["type": "deploy", "cluster": "utv"])
      AuroraConfig auroraConfig = createAuroraConfig(files)

    when:
      AuroraDeploymentConfig auroraDc = service.createAuroraDc(auroraConfig, aid, [], false)
      def auroraDeployDescriptor = (AuroraDeploy) auroraDc.deployDescriptor

    then:
      auroraDeployDescriptor.tag == "default"
  }*/

  def "Should fail due to missing config file"() {

    given:
      Map<String, Map<String, Object>> files = getQaEbsUsersSampleFiles()
      files.remove("${APP_NAME}.json" as String)
      def auroraConfig = createAuroraConfig(files)

    when:
      service.createAuroraDc(auroraConfig, aid, [], false)

    then:
      thrown(IllegalArgumentException)
  }

/* TODO:fix

def "Should successfully merge all config files"() {

given:
  Map<String, Map<String, Object>> files = getQaEbsUsersSampleFiles()
  def auroraConfig = createAuroraConfig(files)

when:
  AuroraDeploymentConfig auroraDc = service.createAuroraDc(auroraConfig, aid, [], false)

then:
  with(auroraDc) {
    namespace == "aos-${ENV_NAME}"
    affiliation == "aos"
    name == APP_NAME
    cluster == "utv"
    replicas == 1
    type == TemplateType.development
    groups.containsAll(["APP_PaaS_drift", "APP_PaaS_utv"])
  }

  with(auroraDc.deployDescriptor as AuroraDeploy) {
    version == "1.0.3-SNAPSHOT"
    groupId == "ske.admin.lisens"
    artifactId == "verify-ebs-users"

    prometheus.port == 8080
    splunkIndex == "openshift-test"
  }
}*/

  def "Should override name property in 'app'.json with name in override"() {

    given:
      Map<String, Map<String, Object>> files = getQaEbsUsersSampleFiles()

      def envAppOverride = """
    {
      "name": "awesome-app"
    }
  """

      files.put("${ENV_NAME}/${APP_NAME}.json" as String, jsonToMap(envAppOverride))

      def auroraConfig = createAuroraConfig(files)

    when:
      AuroraDeploymentConfig auroraDc = service.
          createAuroraDc(auroraConfig, aid, [new AuroraConfigFile("about.json", ["name": "foobar"])], false)

    then:
      auroraDc.name == "foobar"
  }

  def "Should override name property in 'app'.json with name in 'env'/'app'.json"() {

    given:
      Map<String, Map<String, Object>> files = getQaEbsUsersSampleFiles()

      def envAppOverride = """
    {
      "name": "awesome-app"
    }
  """

      files.put("${ENV_NAME}/${APP_NAME}.json" as String, jsonToMap(envAppOverride))

      def auroraConfig = createAuroraConfig(files)

    when:
      AuroraDeploymentConfig auroraDc = service.createAuroraDc(auroraConfig, aid, [], false)

    then:
      auroraDc.name == "awesome-app"
  }

  def "Should throw ValidationException due to missing required properties"() {

    given: "AuroraConfig without build properties"
      Map<String, Map<String, Object>> files = getQaEbsUsersSampleFiles()

      def appOverride = """
    {
      "replicas" : 3,
      "flags" : ["rolling", "route", "cert" ],
      "deploy" : {
        "PROMETHEUS_ENABLED" : true,
        "PROMETHEUS_PORT" : "8081",
        "MANAGEMENT_PATH": ":8081/actuator",
        "DATABASE": "referanseapp"
      }
    }
  """

      files.put("${APP_NAME}.json" as String, jsonToMap(appOverride))
      files.put("${ENV_NAME}/${APP_NAME}.json" as String, [:])

      def auroraConfig = createAuroraConfig(files)

    when:
      service.createAuroraDc(auroraConfig, aid, [], false)

    then:
      def ex = thrown(ApplicationConfigException)
      ex.errors.size() == 3
  }

}

