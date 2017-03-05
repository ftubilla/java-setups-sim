package util;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


public class SerializationTest {

    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        String json = "{}";
        MyClass myClass = mapper.readValue(json, MyClass.class);
        System.out.println(myClass);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "getInstance")
    @ToString
    public static class MyClass {
        @JsonProperty private int value = 10;
    }
    
}
