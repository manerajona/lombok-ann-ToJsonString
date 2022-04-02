package com.manerajona.annotation;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DtoTest {

    @Test
    public void toStringJsonTest() {
        Thing t = new Thing("Example");

        assertEquals("{\"name\":\"Example\"}",  t.toString());
        assertNotEquals("{\"name\":\"Example\"}",  new Thing1("Example").toString());
        assertEquals("{\"id\":1,\"name\":\"Example\",\"thing\":{\"name\":\"Example\"}}",  new ComplexThing(1L, "Example", t).toString());

        assertEquals(new Thing("testEquals"), new Thing("testEquals"));
        assertNotEquals(new Thing1("testEquals"), new Thing1("testEquals"));
    }

    @Dto
    @AllArgsConstructor
    private static class Thing {
        private final String name;
    }

    @AllArgsConstructor
    private static class Thing1 {
        private final String name;
    }

    @Dto
    @AllArgsConstructor
    private static class ComplexThing {
        private final Long id;
        private final String name;
        private final Thing thing;
    }

}
