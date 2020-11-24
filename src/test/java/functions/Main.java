package functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

public class Main {

  public static void main(String[] args)  throws Exception{
    ObjectMapper objectMapper = new ObjectMapper();
    Input input = new Input();
    input.input = "hello";

    CloudEvent cloudEvent = CloudEventBuilder.v1()
                                             .withId(UUID.randomUUID().toString())
                                             .withTime(Instant.now()
                                                              .atOffset(
                                                                  ZoneOffset.UTC))
                                             .withSubject(
                                                 "Convert to UpperCase")
                                             .withData("application/json",
                                                 objectMapper.writeValueAsBytes(
                                                     input))
                                             .withType(
                                                 "com.redhat.faas.springboot.test")
                                             .withSource(URI.create(
                                                 "http://localhost:8080/uppercase"))
                                             .build();

    EventFormat jsonFormat = EventFormatProvider
        .getInstance()
        .resolveFormat(JsonFormat.CONTENT_TYPE);

    byte[] bodyBytes = jsonFormat.serialize(cloudEvent);

    System.out.println(new String(bodyBytes));
  }
}
