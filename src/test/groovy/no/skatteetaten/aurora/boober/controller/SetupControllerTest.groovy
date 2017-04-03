package no.skatteetaten.aurora.boober.controller

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import static no.skatteetaten.aurora.boober.utils.SampleFilesCollector.jsonToMap

import org.springframework.http.MediaType

import groovy.json.JsonSlurper
import no.skatteetaten.aurora.boober.Configuration
import no.skatteetaten.aurora.boober.service.AuroraConfigParserService
import no.skatteetaten.aurora.boober.service.OpenShiftClient
import no.skatteetaten.aurora.boober.service.OpenShiftService
import no.skatteetaten.aurora.boober.service.SetupService
import no.skatteetaten.aurora.boober.utils.SampleFilesCollector

class SetupControllerTest extends AbstractControllerTest {

  public static final String AFFILIATION = "aos"
  public static final String ENV_NAME = "booberdev"
  public static final String APP_NAME = "verify-ebs-users"

  def mapper = new Configuration().mapper()

  def auroraParser = new AuroraConfigParserService()

  def setupService = new SetupService(Stub(OpenShiftService), Stub(OpenShiftClient))
  def controller = new SetupController(setupService, auroraParser)

  def "Should return error"() {
    given:
      def files = SampleFilesCollector.qaEbsUsersSampleFiles
      files.put("booberdev/verify-ebs-users.json", jsonToMap("""
        {
          "build": {
            "VERSION": null
          }
        }
      """))
      SetupCommand cmd = new SetupCommand(AFFILIATION, ENV_NAME, APP_NAME, files, [:])
      def json = mapper.writeValueAsString(cmd)

    when:
      def response = mockMvc
          .perform(put('/setup')
          .header("Authentication", "Bearer test")
          .contentType(MediaType.APPLICATION_JSON)
          .content(json))
      def body = new JsonSlurper().parseText(response.andReturn().response.getContentAsString())

    then:
      body.items[0]["errors"].size() == 1
  }

  @Override
  protected List<Object> getControllersUnderTest() {
    return [controller]
  }
}
