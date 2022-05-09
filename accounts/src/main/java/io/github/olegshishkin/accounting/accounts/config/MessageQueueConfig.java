package io.github.olegshishkin.accounting.accounts.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.olegshishkin.accounting.accounts.jms.JmsErrorHandler;
import java.util.HashMap;
import java.util.Map;
import javax.jms.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@EnableJms
@Configuration
public class MessageQueueConfig {

  private static final String TYPE_ID_PROP = "_type";

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.end-point.uri}")
  private String endPoint;

  @Value("${cloud.aws.credentials.access-key}")
  private String accessKey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String secretKey;

  @Value("${message.concurrency}")
  private String concurrency;

  @Bean
  public AmazonSQSAsync amazonSQSAsync() {
    BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    return AmazonSQSAsyncClientBuilder
        .standard()
        .withRegion(region)
        .withEndpointConfiguration(new EndpointConfiguration(endPoint, region))
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();
  }

  @Bean
  public SQSConnectionFactory connectionFactory(AmazonSQSAsync amazonSQSAsync) {
    return new SQSConnectionFactory(new ProviderConfiguration(), amazonSQSAsync);
  }

  @Bean
  public MappingJackson2MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName(TYPE_ID_PROP);
    converter.setObjectMapper(objectMapper);
    return converter;
  }

  @Bean
  public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
      SQSConnectionFactory connectionFactory,
      JmsErrorHandler errorHandler,
      MappingJackson2MessageConverter messageConverter) {

    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
    factory.setErrorHandler(errorHandler);
    factory.setMessageConverter(messageConverter);
    factory.setConcurrency(concurrency);
    return factory;
  }

  @ConfigurationProperties("message.queue")
  @Bean
  public Map<String, String> queueNames() {
    return new HashMap<>();
  }
}