package com.example.PlanItPoker.payload.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WsResponse {
    private String type;
    private Object data;

    // Constructor
    public WsResponse(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    // Getters
    public String getType() { return type; }
    public Object getData() { return data; }
}
