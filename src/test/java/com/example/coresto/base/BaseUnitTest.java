package com.example.coresto.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Global base class for fast, isolated Unit Tests.
 *
 * <p>Features:
 * <ul>
 *   <li>Enables Mockito via {@link ExtendWith @ExtendWith(MockitoExtension.class)}.</li>
 *   <li>Does NOT load the Spring ApplicationContext for instant execution.</li>
 *   <li>Provides pre-configured {@link ObjectMapper} and JSON serialization/deserialization helpers.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * class OrderServiceTest extends BaseUnitTest {
 *     @Mock
 *     private OrderRepository orderRepository;
 *
 *     @InjectMocks
 *     private OrderService orderService;
 *
 *     @Test
 *     void testCreateOrder() {
 *         // test logic
 *     }
 * }
 * }</pre>
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest {

    protected static final ObjectMapper objectMapper = createDefaultObjectMapper();

    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Serializes any Java object to its JSON string representation.
     */
    protected String asJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object to JSON: " + object, e);
        }
    }

    /**
     * Deserializes a JSON string into an instance of the specified class.
     */
    protected <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to " + clazz.getSimpleName() + ": " + json, e);
        }
    }

    /**
     * Deserializes a JSON string into a complex generic type (e.g. {@code List<MyDto>}).
     */
    protected <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize JSON to generic type: " + json, e);
        }
    }
}
