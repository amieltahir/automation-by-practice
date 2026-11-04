package io.trscore.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloControllerTest {

    @Test
    void helloReturnsServiceName() {
        assertEquals("trs-demo-api", new HelloController().hello().get("service"));
    }
}