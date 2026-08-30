package ru.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.analyzer.service.EventsSimilarityProcessor;
import ru.practicum.analyzer.service.UserActionProcessor;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyzer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyzer.class, args);

        final UserActionProcessor userActionProcessor = context.getBean(UserActionProcessor.class);
        final EventsSimilarityProcessor eventsSimilarityProcessor = context.getBean(EventsSimilarityProcessor.class);

        Thread userActionThread = new Thread(userActionProcessor);
        userActionThread.setName("UserActionHandlerThread");
        userActionThread.start();

        Thread eventsSimilarityThread = new Thread(eventsSimilarityProcessor);
        eventsSimilarityThread.setName("EventsSimilarityHandlerThread");
        eventsSimilarityThread.start();
    }
}
