package plugin.borealcore.functions.configeditor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ValueConverter {

    public static Object convertValue(String input, Object originalValue) {
        if (originalValue instanceof String) {
            return input;
        } else if (originalValue instanceof Integer) {
            return Integer.parseInt(input);
        } else if (originalValue instanceof Double) {
            return Double.parseDouble(input);
        } else if (originalValue instanceof Float) {
            return Float.parseFloat(input);
        } else if (originalValue instanceof Long) {
            return Long.parseLong(input);
        } else if (originalValue instanceof Boolean) {
            return Boolean.parseBoolean(input);
        } else if (originalValue instanceof List) {
            // Parse list from comma-separated values
            List<?> originalList = (List<?>) originalValue;
            if (originalList.isEmpty()) {
                // If list is empty, create a list of strings
                return Arrays.asList(input.split(",\\s*"));
            } else {
                // Otherwise, use the type of the first element
                Object firstElement = originalList.get(0);
                String[] parts = input.split(",\\s*");

                if (firstElement instanceof String) {
                    return Arrays.asList(parts);
                } else if (firstElement instanceof Integer) {
                    return Arrays.stream(parts).map(Integer::parseInt).collect(Collectors.toList());
                } else if (firstElement instanceof Double) {
                    return Arrays.stream(parts).map(Double::parseDouble).collect(Collectors.toList());
                } else if (firstElement instanceof Boolean) {
                    return Arrays.stream(parts).map(Boolean::parseBoolean).collect(Collectors.toList());
                } else {
                    return Arrays.asList(parts); // Default to strings
                }
            }
        } else {
            // For unknown types, return as string
            return input;
        }
    }
}