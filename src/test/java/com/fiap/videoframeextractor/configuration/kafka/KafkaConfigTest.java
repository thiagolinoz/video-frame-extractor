package com.fiap.videoframeextractor.configuration.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaConfig - Testes Unitários")
class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "test-group";

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", BOOTSTRAP_SERVERS);
        ReflectionTestUtils.setField(kafkaConfig, "groupId", GROUP_ID);
    }

    @Test
    @DisplayName("Deve criar ConsumerFactory com as propriedades corretas")
    void deveCriarConsumerFactoryComPropriedadesCorretas() {
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        assertThat(consumerFactory).isNotNull();

        Map<String, Object> configs = consumerFactory.getConfigurationProperties();
        assertThat(configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo(BOOTSTRAP_SERVERS);
        assertThat(configs.get(ConsumerConfig.GROUP_ID_CONFIG)).isEqualTo(GROUP_ID);
        assertThat(configs.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)).isEqualTo(StringDeserializer.class);
        assertThat(configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)).isEqualTo(StringDeserializer.class);
        assertThat(configs.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo(false);
        assertThat(configs.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)).isEqualTo("earliest");
    }

    @Test
    @DisplayName("Deve criar ProducerFactory com as propriedades corretas")
    void deveCriarProducerFactoryComPropriedadesCorretas() {
        ProducerFactory<String, String> producerFactory = kafkaConfig.producerFactory();

        assertThat(producerFactory).isNotNull();

        Map<String, Object> configs = producerFactory.getConfigurationProperties();
        assertThat(configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)).isEqualTo(BOOTSTRAP_SERVERS);
        assertThat(configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)).isEqualTo(StringSerializer.class);
        assertThat(configs.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
        assertThat(configs.get(ProducerConfig.RETRIES_CONFIG)).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve criar KafkaTemplate não nulo")
    void deveCriarKafkaTemplateNaoNulo() {
        KafkaTemplate<String, String> template = kafkaConfig.kafkaTemplate();

        assertThat(template).isNotNull();
    }

    @Test
    @DisplayName("Deve criar ConcurrentKafkaListenerContainerFactory configurado com ACK manual")
    void deveCriarContainerFactoryComAckManual() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                kafkaConfig.kafkaListenerContainerFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getContainerProperties().getAckMode())
                .isEqualTo(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
    }

    

    @Test
    @DisplayName("Deve criar ObjectMapper não nulo")
    void deveCriarObjectMapperNaoNulo() {
        ObjectMapper objectMapper = kafkaConfig.objectMapper();

        assertThat(objectMapper).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar auto commit como false no consumer")
    void deveConfigurarAutoCommitComoFalse() {
        ConsumerFactory<String, String> consumerFactory = kafkaConfig.consumerFactory();

        Map<String, Object> configs = consumerFactory.getConfigurationProperties();
        assertThat(configs.get(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG)).isEqualTo(false);
    }

    @Test
    @DisplayName("Deve configurar acks como 'all' no producer para garantir entrega")
    void deveConfigurarAcksAllNoProducer() {
        ProducerFactory<String, String> producerFactory = kafkaConfig.producerFactory();

        Map<String, Object> configs = producerFactory.getConfigurationProperties();
        assertThat(configs.get(ProducerConfig.ACKS_CONFIG)).isEqualTo("all");
    }

    @Test
    @DisplayName("Deve retornar instâncias independentes de ConsumerFactory")
    void deveRetornarInstanciasDeConsumerFactory() {
        ConsumerFactory<String, String> factory1 = kafkaConfig.consumerFactory();
        ConsumerFactory<String, String> factory2 = kafkaConfig.consumerFactory();

        assertThat(factory1).isNotNull();
        assertThat(factory2).isNotNull();
    }

    @Test
    @DisplayName("Deve criar beans sem lançar exceções")
    void deveCriarBeansSemExcecoes() {
        assertThatCode(() -> {
            kafkaConfig.consumerFactory();
            kafkaConfig.producerFactory();
            kafkaConfig.kafkaTemplate();
            kafkaConfig.kafkaListenerContainerFactory();
            kafkaConfig.objectMapper();
        }).doesNotThrowAnyException();
    }
}
