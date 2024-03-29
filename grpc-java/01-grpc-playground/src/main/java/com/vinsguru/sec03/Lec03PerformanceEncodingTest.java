package com.vinsguru.sec03;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.vinsguru.models.sec03.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Lec03PerformanceEncodingTest {

    private static final Logger log = LoggerFactory.getLogger(Lec03PerformanceEncodingTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {

        var protoPerson = Person.newBuilder() 
                           .setEmployed(false)
                           .build();
        var jsonPerson = new JsonPersonV1(false);
        json(jsonPerson);
        proto(protoPerson);

//        for (int i = 0; i < 5; i++) {
//            runTest("json", () -> json(jsonPerson));
//            runTest("proto", () -> proto(protoPerson));
//        }

    }

    private static void proto(Person person){
        try {
            var bytes = person.toByteArray();
            log.info("proto bytes length: {}", bytes.length);
            Person.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static void json(JsonPersonV1 person){
        try{
            var bytes = mapper.writeValueAsBytes(person);
            log.info("json bytes length: {}", bytes.length);
            mapper.readValue(bytes, JsonPerson.class);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private static void runTest(String testName, Runnable runnable){
        var start = System.currentTimeMillis();
        for (int i = 0; i < 5_000_000; i++) {
            runnable.run();
        }
        var end = System.currentTimeMillis();
        log.info("time taken for {} - {} ms", testName, (end - start));
    }

}
