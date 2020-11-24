package functions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import java.net.URI;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

@SpringBootApplication
public class SpringCloudEventsApplication {

  private static final Logger LOGGER = Logger.getLogger(
      SpringCloudEventsApplication.class.getName());

  final ObjectMapper objectMapper;

  public static void main(String[] args) {
    SpringApplication.run(SpringCloudEventsApplication.class, args);
  }

  public SpringCloudEventsApplication(@Autowired ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Bean
  public Function<Message<String>, String> uppercase() {

    return m -> {
      byte[] jsonResponse = "{}".getBytes();

      String payload = m.getPayload();

      LOGGER.log(Level.INFO, "Received Cloud Event {0} ", payload);

      EventFormat jsonFormat = EventFormatProvider
          .getInstance()
          .resolveFormat(JsonFormat.CONTENT_TYPE);

      final Output response = new Output();

      try {

        if (jsonFormat != null) {

          CloudEvent inputEvent = jsonFormat.deserialize(payload.getBytes());
          if (inputEvent != null) {
            LOGGER.log(Level.INFO, "Input CE Id:{0}", inputEvent.getId());
            LOGGER.log(Level.INFO, "Input CE Spec Version:{0}",
                inputEvent.getSpecVersion());
            LOGGER.log(Level.INFO, "Input CE Source:{0}",
                inputEvent.getSource());
            LOGGER.log(Level.INFO, "Input CE Subject:{0}",
                inputEvent.getSubject());

            CloudEventData inputData = inputEvent.getData();
            if (inputData != null && "application/json".equals(
                inputEvent.getDataContentType())) {

              byte[] ibytes = inputData.toBytes();

              Input ucRequest = objectMapper.readValue(ibytes,
                  Input.class);

              LOGGER.info("CE Data :" + new String(ibytes));

              //POJO with JSON Representation

              response.input = ucRequest.input;
              response.operation = inputEvent.getSubject();
              response.output = ucRequest.input.toUpperCase();

            } else {
              response.input = "Error";
              response.operation = inputEvent.getSubject();
              response.output = "Error";
              response.error = "Unsupported content type, only application/json is supported";
            }
          }
        }
        //Serialize the response as JSON Bytes
        jsonResponse = objectMapper.writeValueAsBytes(response);
      } catch (JsonProcessingException e) {
        LOGGER.log(Level.SEVERE, "Unable to marshall response", e);
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Error processing request", e);
      }

      final CloudEvent responseCloudEvent = CloudEventBuilder.v1()
                                                             .withId("0000")
                                                             .withType(
                                                                 "com.redhat.faas.springboot.events")
                                                             .withSource(
                                                                 URI.create(
                                                                     "http://localhost:8080/uppercase"))
                                                             .withData(
                                                                 jsonResponse)
                                                             .build();

      var strCloudEvent = new String(jsonFormat.serialize(responseCloudEvent));
      LOGGER.log(Level.INFO, "Sending Cloud Event {0} ", strCloudEvent);
      return strCloudEvent;
    };
  }
}
