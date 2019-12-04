package org.eclipse.keyple.plugin.android.cone2;

import java.util.Map;

/**
 * Utility class to handle parameters
 */
public class Cone2ParametersUtils {
    /**
     * Gets a parameter stored as String and converts it to Integer.
     * If parameter has been incorrectly stored as non-Integer, default value will be used.
     * If default value is non-integer, returns 0.
     * @param param Parameter key
     * @param defaultValue Default parameter value
     * @return Parameter value, as an integer
     */
    static int getIntParam(Map<String, String> parameters, String param, String defaultValue) throws NumberFormatException {
        try {
            return Integer.parseInt(parameters.get(param));
        } catch (NumberFormatException nfe) {
            return Integer.parseInt(defaultValue);
        }
    }
}
