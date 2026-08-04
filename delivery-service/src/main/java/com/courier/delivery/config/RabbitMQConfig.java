package com.courier.delivery.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

	@Bean
	public TopicExchange deliveryExchange() {
		return new TopicExchange(exchangeName, true, false);
	}

	@Bean
	public MessageConverter jacksonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(
			ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jacksonMessageConverter);
		template.setExchange(exchangeName);
		return template;
	}
}
