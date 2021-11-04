package com.MohafizDZ.framework_repository.service;

import java.util.ArrayList;
import java.util.List;

public class OrderBy {
    private List<OrderByLine> orderByLines = new ArrayList<>();

    public OrderBy addLine(OrderByLine orderByLine){
        orderByLines.add(orderByLine);
        return this;
    }

    public String generateOrderBy(){
        StringBuilder stringBuilder = new StringBuilder();
        for(OrderByLine orderByLine : orderByLines){
            stringBuilder.append("\n");
            stringBuilder.append("      {\n");
            stringBuilder.append("        \"field\": {");
            stringBuilder.append("          \"fieldPath\": \"").
                    append(orderByLine.getField()).append("\"\n");
            stringBuilder.append("        },\n");
            stringBuilder.append("        \"direction\": \"").
                    append(getDirection(orderByLine.getDirection())).append("\"\n");
            stringBuilder.append("      },");
        }
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(","));
        return stringBuilder.toString();
    }

    private String getDirection(OrderByLine.Direction direction){
        return direction.name();
    }
}
