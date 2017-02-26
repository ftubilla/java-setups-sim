package util;

import java.util.Optional;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;


public class SerializationTest {

    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        Optional<Integer> hola1 = Optional.of(1);
        String json = mapper.writeValueAsString(hola1);
        System.out.println("JSON: " + json);
        Integer hola = mapper.readValue(json, Integer.class);
        System.out.println("I read: " + hola);
    }

}
