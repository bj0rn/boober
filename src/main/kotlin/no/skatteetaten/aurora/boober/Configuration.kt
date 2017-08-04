package no.skatteetaten.aurora.boober

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.encryptor4j.factory.AbsKeyFactory
import org.encryptor4j.factory.KeyFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.security.cert.X509Certificate
import java.util.*


@Configuration
class Configuration {


    @Bean
    @Primary
    fun mapper(): ObjectMapper {
        return jacksonObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    @Bean
    fun velocity(): VelocityEngine {
        return VelocityEngine().apply {
            setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
            setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
            init()
        }
    }

    @Bean
    fun keyFactory(): KeyFactory = object : AbsKeyFactory("AES", 128) {}

    @Bean
    @Primary
    fun restTemplate(): RestTemplate {

        fun createSslTrustAllHttpClient(): CloseableHttpClient? {
            val acceptingTrustStrategy = { chain: Array<X509Certificate>, authType: String -> true }

            val sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, acceptingTrustStrategy)
                    .build()

            val csf = SSLConnectionSocketFactory(sslContext)

            val httpClient = HttpClients.custom()
                    .setSSLSocketFactory(csf)
                    .build()
            return httpClient
        }

        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory().apply {
            setReadTimeout(2000)
            setConnectTimeout(2000)
            httpClient = createSslTrustAllHttpClient()
        }

        val myRest = RestTemplate(clientHttpRequestFactory)

        for (myConverter in myRest.messageConverters) {
            if (myConverter is MappingJackson2HttpMessageConverter) {
                val myMediaTypes = ArrayList<MediaType>()
                myMediaTypes.addAll(myConverter.getSupportedMediaTypes())
                myMediaTypes.add(MediaType.parseMediaType("application/vnd.docker.distribution.manifest.v1+prettyjws"))
                myConverter.supportedMediaTypes = myMediaTypes
            }
        }
        return myRest

    }
}