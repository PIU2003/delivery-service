package com.courier.courier.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitMQConfig {

	@Value("${app.rabbitmq.exchange}")
	private String exchangeName;

	@Value("${app.rabbitmq.queue}")
	private String queueName;

	@Value("${app.rabbitmq.routing-keys.assigned}")
	private String assignedKey;

	@Value("${app.rabbitmq.routing-keys.delivered}")
	private String deliveredKey;

	@Bean
	public TopicExchange deliveryExchange() {
		return new TopicExchange(exchangeName, true, false);
	}

	@Bean
	public Queue courierAvailabilityQueue() {
		return new Queue(queueName, true);
	}

	@Bean
	public Binding assignedBinding(Queue courierAvailabilityQueue, TopicExchange deliveryExchange) {
		return BindingBuilder.bind(courierAvailabilityQueue).to(deliveryExchange).with(assignedKey);
	}

	@Bean
	public Binding deliveredBinding(Queue courierAvailabilityQueue, TopicExchange deliveryExchange) {
		return BindingBuilder.bind(courierAvailabilityQueue).to(deliveryExchange).with(deliveredKey);
	}

	@Bean
	public MessageConverter jacksonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(jacksonMessageConverter);
		return factory;
	}
}
