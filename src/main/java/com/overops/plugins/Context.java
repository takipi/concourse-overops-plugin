package com.overops.plugins;

import com.overops.plugins.service.OutputWriter;

public class Context {
    private OutputWriter outputStream;

    public OutputWriter getOutputStream() {
        return outputStream;
    }

    public static ContextBuilder getBuilder() {
        return new Context().new ContextBuilder();
    }

    public class ContextBuilder {
        private ContextBuilder() {

        }

        public ContextBuilder setOutpitStream(OutputWriter outputStream) {
            Context.this.outputStream = outputStream;
            return this;
        }

        public Context build() {
            return Context.this;
        }
    }
}
