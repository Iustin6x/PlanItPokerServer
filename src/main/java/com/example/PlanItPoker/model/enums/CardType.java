package com.example.PlanItPoker.model.enums;

import java.util.Collections;
import java.util.List;

public enum CardType {
    SEQUENTIAL("sequential"),
    FIBONACCI("fibonacci"),
    HOURS("hours"),
    CUSTOM("custom");

    private final String typeName;

    CardType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}

