package uk.gov.justice.digital.hmpps.whereabouts.config

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service


@Configuration
@ConditionalOnProperty(name = ["sqs.provider"], havingValue = "embedded-localstack")
class JmsLocalStackConfig(private val localStackContainer: LocalStackContainer) {

  @Bean
  fun awsSqsClient(): AmazonSQS = AmazonSQSClientBuilder.standard()
      .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(Service.SQS))
      .withCredentials(localStackContainer.defaultCredentialsProvider)
      .build()

  @Bean
  fun awsSqsDlqClient(): AmazonSQS = AmazonSQSClientBuilder.standard()
      .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(Service.SQS))
      .withCredentials(localStackContainer.defaultCredentialsProvider)
      .build()
}
