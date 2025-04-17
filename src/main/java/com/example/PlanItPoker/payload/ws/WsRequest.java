package com.example.PlanItPoker.payload.ws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
public class WsRequest {
    private String type;
    private Map<String, String> data;

    // Getters și Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Map<String, String> getData() { return data; }
    public void setData(Map<String, String> data) { this.data = data; }
}
