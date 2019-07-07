package com.overops.plugins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.overops.plugins.service.OutputWriter;

import java.util.Optional;

public class Context {
    private OutputWriter outputStream;

    private ObjectMapper objectMapper;

    public OutputWriter getOutputStream() {
        return outputStream;
    }

    public ObjectMapper getObjectMapper() {
        return Optional.ofNullable(objectMapper).orElse(new ObjectMapper());
    }

    public static ContextBuilder getBuilder() {
        return new Context().new ContextBuilder();
    }

    public class ContextBuilder {
        private ContextBuilder() {

        }

        public ContextBuilder setOutputStream(OutputWriter outputStream) {
            Context.this.outputStream = outputStream;
            return this;
        }

        public ContextBuilder setObjectMapper(ObjectMapper objectMapper) {
            Context.this.objectMapper = objectMapper;
            return this;
        }


        public Context build() {
            return Context.this;
        }
    }
}
