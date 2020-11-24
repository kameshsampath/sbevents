package functions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = SpringCloudEventsApplication.class,
    webEnvironment = WebEnvironment.RANDOM_PORT)
public class SpringCloudEventsApplicationTests {

  @Autowired
  private TestRestTemplate rest;

  @Autowired
  ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testUpperCaseJsonInput() throws Exception {

    Input input = new Input();
    input.input = "hello";

    CloudEvent cloudEvent = CloudEventBuilder.v1()
                                             .withId("1111")
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

    ResponseEntity<String> response = this.rest.exchange(
        RequestEntity.post(new URI("/uppercase"))
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(bodyBytes), String.class);

    assertThat(response.getStatusCode()
                       .value(), equalTo(200));
    String body = response.getBody();
    assertThat(body, notNullValue());
    CloudEvent ce = jsonFormat.deserialize(body.getBytes());
    assertThat(ce, notNullValue());
    Output output = objectMapper.readValue(ce.getData()
                                             .toBytes(),
        Output.class);
    assertThat(output, notNullValue());
    assertThat(output.input, equalTo("hello"));
    assertThat(output.operation, equalTo("Convert to UpperCase"));
    assertThat(output.output, equalTo("HELLO"));
    assertThat(output.error, nullValue());
  }

  @Test
  public void testUpperCaseInvalidInput() throws Exception {

    CloudEvent cloudEvent = CloudEventBuilder.v1()
                                             .withId("1111")
                                             .withSubject(
                                                 "Convert to UpperCase")
                                             .withData("text/plain",
                                                 "hello".getBytes())
                                             .withType(
                                                 "com.redhat.faas.springboot.test")
                                             .withSource(URI.create(
                                                 "http://localhost:8080/uppercase"))
                                             .build();

    EventFormat jsonFormat = EventFormatProvider
        .getInstance()
        .resolveFormat(JsonFormat.CONTENT_TYPE);

    byte[] bodyBytes = jsonFormat.serialize(cloudEvent);

    //System.out.println(new String(bodyBytes));

    ResponseEntity<String> response = this.rest.exchange(
        RequestEntity.post(new URI("/uppercase"))
                     .contentType(MediaType.APPLICATION_JSON)
                     .body(bodyBytes), String.class);

    assertThat(response.getStatusCode()
                       .value(), equalTo(200));
    String body = response.getBody();
    assertThat(body, notNullValue());
    CloudEvent ce = jsonFormat.deserialize(body.getBytes());
    assertThat(ce, notNullValue());
    Output output = objectMapper.readValue(ce.getData()
                                             .toBytes(),
        Output.class);
    assertThat(output, notNullValue());
    assertThat(output.input, equalTo("Error"));
    assertThat(output.operation, equalTo("Convert to UpperCase"));
    assertThat(output.output, equalTo("Error"));
    assertThat(output.error, equalTo(
        "Unsupported content type, only application/json is supported"));
  }
}
