package com.example.axonssejavascript;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.extensions.mongo.DefaultMongoTemplate;
import org.axonframework.extensions.mongo.MongoTemplate;
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.MessageOriginProvider;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

  @Autowired
  public void configure(ObjectMapper objectMapper) {
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

//  @Autowired
//  public void configure(EventProcessingConfigurer configurer) {
//    // This will configure all tracking processors
//    configurer.registerDefaultListenerInvocationErrorHandler(c -> PropagatingErrorHandler.INSTANCE);
//  }

  @Bean
  public TokenStore tokenStore(Serializer serializer, MongoClient client) {
    return MongoTokenStore.builder().mongoTemplate(mongoTemplate(client)).serializer(serializer).build();
  }
  //
  // @Bean
  // public SagaStore<Object> sagaStore(Serializer serializer, MongoClient client) {
  // return MongoSagaStore.builder().mongoTemplate(mongoTemplate(client)).serializer(serializer).build();
  // }

  MongoTemplate mongoTemplate(MongoClient client) {
    return DefaultMongoTemplate.builder().mongoDatabase(client).build();
  }

  @Bean
  public CorrelationDataProvider messageOriginProvider() {
    return new MessageOriginProvider();
  }
}
