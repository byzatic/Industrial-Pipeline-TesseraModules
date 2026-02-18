package io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto;

import io.github.byzatic.tessera.storageapi.dto.DataValueInterface;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class DataItem implements DataValueInterface {
    private String metricName;
    private String metricValue;
    private Instant metricCreationTime;
    private List<MetricLabel> metricLabels;

    public DataItem() {
    }

    private DataItem(Builder builder) {
        metricName = builder.metricName;
        metricValue = builder.metricValue;
        metricCreationTime = builder.metricCreationTime;
        metricLabels = builder.metricLabels;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(DataItem copy) {
        Builder builder = new Builder();
        builder.metricName = copy.getMetricName();
        builder.metricValue = copy.getMetricValue();
        builder.metricCreationTime = copy.getMetricCreationTime();
        builder.metricLabels = copy.getMetricLabels();
        return builder;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getMetricValue() {
        return metricValue;
    }

    public Instant getMetricCreationTime() {
        return metricCreationTime;
    }

    public List<MetricLabel> getMetricLabels() {
        return metricLabels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataItem dataItem = (DataItem) o;
        return Objects.equals(metricName, dataItem.metricName) && Objects.equals(metricValue, dataItem.metricValue) && Objects.equals(metricCreationTime, dataItem.metricCreationTime) && Objects.equals(metricLabels, dataItem.metricLabels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricName, metricValue, metricCreationTime, metricLabels);
    }

    @Override
    public String toString() {
        return "DataItem{" +
                "metricName='" + metricName + '\'' +
                ", metricValue='" + metricValue + '\'' +
                ", metricCreationTime=" + metricCreationTime +
                ", metricLabels=" + metricLabels +
                '}';
    }

    /**
     * {@code DataItem} builder static inner class.
     */
    public static final class Builder {
        private String metricName;
        private String metricValue;
        private Instant metricCreationTime;
        private List<MetricLabel> metricLabels;

        private Builder() {
        }

        /**
         * Sets the {@code metricName} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param metricName the {@code metricName} to set
         * @return a reference to this Builder
         */
        public Builder setMetricName(String metricName) {
            this.metricName = metricName;
            return this;
        }

        /**
         * Sets the {@code metricValue} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param metricValue the {@code metricValue} to set
         * @return a reference to this Builder
         */
        public Builder setMetricValue(String metricValue) {
            this.metricValue = metricValue;
            return this;
        }

        /**
         * Sets the {@code metricCreationTime} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param metricCreationTime the {@code metricCreationTime} to set
         * @return a reference to this Builder
         */
        public Builder setMetricCreationTime(Instant metricCreationTime) {
            this.metricCreationTime = metricCreationTime;
            return this;
        }

        /**
         * Sets the {@code metricLabels} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param metricLabels the {@code metricLabels} to set
         * @return a reference to this Builder
         */
        public Builder setMetricLabels(List<MetricLabel> metricLabels) {
            this.metricLabels = metricLabels;
            return this;
        }

        /**
         * Returns a {@code DataItem} built from the parameters previously set.
         *
         * @return a {@code DataItem} built with parameters of this {@code DataItem.Builder}
         */
        public DataItem build() {
            return new DataItem(this);
        }
    }
}
