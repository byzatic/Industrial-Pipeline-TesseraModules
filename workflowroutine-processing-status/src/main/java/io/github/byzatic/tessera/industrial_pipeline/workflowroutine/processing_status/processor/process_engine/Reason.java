package io.github.byzatic.tessera.industrial_pipeline.workflowroutine.processing_status.processor.process_engine;

import io.github.byzatic.tessera.industrial_pipeline.sharedresources.project_common.dto.MetricLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Reason {
    private final static Logger logger = LoggerFactory.getLogger(Reason.class);
    private final String key = "reason";
    private final String sign = "=";
    private String value = "Null";

    public Reason() {
    }

    public void add(String reason) {
        logger.debug("Add reason; reason= {}", reason);
        if (value.equals("Null")) {
            logger.debug("Add reason; current value equals Null - override");
            value = reason;
        } else {
            logger.debug("Add reason; current value not equals Null - concatenate");
            value = value + " *|* " + reason;
        }
    }

    public MetricLabel getMetricLabel() {
        return MetricLabel.newBuilder()
                .setKey(key)
                .setSign(sign)
                .setValue(value)
                .build();
    }

    public void setEmpty() {
        if (value.equals("Null")) {
            logger.debug("Set Empty reason; current value equals Null - set");
            value = "Null";
        } else {
            logger.debug("Set Empty reason; current value not equals Null - pass");
        }
    }
}
