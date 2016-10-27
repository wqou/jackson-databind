package com.fasterxml.jackson.databind.struct;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;

/**
 * Unit tests for "POJO as array" feature using Builder-style
 * POJO construction.
 */
public class TestPOJOAsArrayWithBuilder extends BaseMapTest
{
    @JsonDeserialize(builder=SimpleBuilderXY.class)
    @JsonFormat(shape=JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(alphabetic=true)
    static class ValueClassXY
    {
        final int _x, _y;

        protected ValueClassXY(int x, int y) {
            _x = x+1;
            _y = y+1;
        }
    }

    @JsonFormat(shape=JsonFormat.Shape.ARRAY)
    static class SimpleBuilderXY
    {
        public int x, y;

        protected SimpleBuilderXY() { }
        protected SimpleBuilderXY(int x0, int y0) {
            x = x0;
            y = y0;
        }
        
        public SimpleBuilderXY withX(int x0) {
            this.x = x0;
            return this;
        }

        public SimpleBuilderXY withY(int y0) {
            this.y = y0;
            return this;
        }

        public ValueClassXY build() {
            return new ValueClassXY(x, y);
        }
    }

    // Also, with creator:

    @JsonDeserialize(builder=CreatorBuilder.class)
    @JsonFormat(shape=JsonFormat.Shape.ARRAY)
    @JsonPropertyOrder(alphabetic=true)
    static class CreatorValue
    {
        final int a, b, c;

        protected CreatorValue(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }
    }

    @JsonFormat(shape=JsonFormat.Shape.ARRAY)
    static class CreatorBuilder {
        private final int a, b;
        private int c;

        @JsonCreator
        public CreatorBuilder(@JsonProperty("a") int a,
                @JsonProperty("b") int b)
        {
            this.a = a;
            this.b = b;
        }
        
        public CreatorBuilder withC(int v) {
            c = v;
            return this;
        }
        public CreatorValue build() {
            return new CreatorValue(a, b, c);
        }
    }

    /*
    /*****************************************************
    /* Basic tests
    /*****************************************************
     */

    private final static ObjectMapper MAPPER = new ObjectMapper();

    public void testSimpleBuilder() throws Exception
    {
        // Ok, first, ensure that serializer will "black out" filtered properties
        ValueClassXY value = MAPPER.readValue("[1,2]", ValueClassXY.class);
        assertEquals(2, value._x);
        assertEquals(3, value._y);
    }

    // Won't work, but verify exception
    public void testBuilderWithUpdate() throws Exception
    {
        // Ok, first, simple case of all values being present
        try {
            /*value =*/ MAPPER.readerFor(ValueClassXY.class)
                    .withValueToUpdate(new ValueClassXY(6, 7))
                    .readValue("[1,2]");
            fail("Should not pass");
        } catch (InvalidDefinitionException e) {
            verifyException(e, "Deserialization of");
            verifyException(e, "by passing existing instance");
            verifyException(e, "ValueClassXY");
        }
    }

    /*
    /*****************************************************
    /* Creator test(s)
    /*****************************************************
     */
    
    // test to ensure @JsonCreator also works
    public void testWithCreator() throws Exception
    {
        final String json = "[1,2,3]";
        CreatorValue value = MAPPER.readValue(json, CreatorValue.class);        
        assertEquals(1, value.a);
        assertEquals(2, value.b);
        assertEquals(3, value.c);
    }
}
