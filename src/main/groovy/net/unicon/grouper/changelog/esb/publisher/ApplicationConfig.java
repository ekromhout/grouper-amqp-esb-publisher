package net.unicon.grouper.changelog.esb.publisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.StringUtils;

@Configuration
@PropertySource("classpath:grouper-loader.properties")
public class ApplicationConfig {

    @Bean
    public AmqpTemplate amqpTemplate(@Value("${changeLog.consumer.esbAmqp.hostName}")
                                     String hostName,
                                     @Value("${changeLog.consumer.esbAmqp.defaultExchange}")
                                     String defaultExchange,
                                     @Value("${changeLog.consumer.esbAmqp.username}")
                                     String username,
                                     @Value("${changeLog.consumer.esbAmqp.password}")
                                     String password) {
        CachingConnectionFactory factory = new CachingConnectionFactory(hostName);
        factory.setUsername(username);
        factory.setPassword(password);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        rabbitTemplate.setExchange(defaultExchange);
        return rabbitTemplate;
    }

    @Bean
    public AmqpRoutingKeyCreator amqpRoutingKeyCreator(@Value("${changeLog.consumer.esbAmqp.regexReplacementDefinition:}")
                                                       String regexReplacementDefinition,
                                                       @Value("${changeLog.consumer.esbAmqp.replaceRoutingKeyColonsWithPeriods:}")
                                                       String replaceColonsWithPeriods) {

        final boolean replaceColons = (StringUtils.hasText(replaceColonsWithPeriods) && "true".equals(replaceColonsWithPeriods));
        if (StringUtils.hasText(regexReplacementDefinition)) {
            return new SpelRegexReplacementBasedRoutingKeyCreator(replaceColons, regexReplacementDefinition);
        }
        else {
            return new GroupNameBasedRoutingKeyCreator(replaceColons);
        }
    }

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
