package io.github.olegshishkin.accounting.accounts.jms;

import java.util.Map;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public record MessageSender(JmsTemplate jmsTemplate, Map<String, String> queueNames) {

  public void send(Object message) {
    jmsTemplate.convertAndSend(getQueue(message), message);
  }

  private String getQueue(Object message) {
    return queueNames.get(message.getClass().getSimpleName());
  }
}
