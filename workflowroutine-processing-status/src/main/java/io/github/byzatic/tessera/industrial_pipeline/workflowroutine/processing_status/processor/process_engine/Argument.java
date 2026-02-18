package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.processor.process_engine;

import java.util.Objects;

public class Argument {
    private String key;
    private String value;

    public Argument() {
    }

    private Argument(Builder builder) {
        key = builder.key;
        value = builder.value;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Argument copy) {
        Builder builder = new Builder();
        builder.key = copy.getKey();
        builder.value = copy.getValue();
        return builder;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Argument argument = (Argument) o;
        return Objects.equals(key, argument.key) && Objects.equals(value, argument.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "Argument{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    /**
     * {@code Argument} builder static inner class.
     */
    public static final class Builder {
        private String key;
        private String value;

        private Builder() {
        }

        /**
         * Sets the {@code key} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param key the {@code key} to set
         * @return a reference to this Builder
         */
        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        /**
         * Sets the {@code value} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param value the {@code value} to set
         * @return a reference to this Builder
         */
        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        /**
         * Returns a {@code Argument} built from the parameters previously set.
         *
         * @return a {@code Argument} built with parameters of this {@code Argument.Builder}
         */
        public Argument build() {
            return new Argument(this);
        }
    }
}
