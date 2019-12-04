package org.eclipse.keyple.plugin.android.cone2;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Cone2ParametersUtilsTest {

    private HashMap<String, String> parameters = new HashMap<String, String>();

    private static final String PARAM_1_KEY = "PARAM_1_KEY";
    private static final int PARAM_1_VALUE = 500;
    private static final int PARAM_1_DEFAULT_VALUE = 750;
    private static final String PARAM_2_KEY = "PARAM_2_KEY";
    private static final String PARAM_2_VALUE = "PARAM_2_VALUE";
    private static final String PARAM_3_KEY = "PARAM_3_KEY";
    private static final String PARAM_3_VALUE = "Incorrect value";
    private static final int PARAM_3_DEFAULT_VALUE = 750;


    @Before
    public void before() {
        // We set parameters to default values
        parameters.put(PARAM_1_KEY, Integer.toString(PARAM_1_VALUE));
        parameters.put(PARAM_2_KEY, PARAM_2_VALUE);
        parameters.put(PARAM_3_KEY, PARAM_3_VALUE);
    }

    @Test
    public void getIntParamTest() {
        // 1 - Tests regular case: parameter stored value is an integer different from default value
        // Expected result is stored value as an integer
        assertThat(Cone2ParametersUtils.getIntParam(parameters,
                PARAM_1_KEY,
                Integer.toString(PARAM_1_DEFAULT_VALUE)),
                is(PARAM_1_VALUE));

        // 2 - Tests error case when value stored is not an integer
        try {
            Cone2ParametersUtils.getIntParam(parameters, PARAM_2_KEY, PARAM_2_VALUE);
            fail();
        } catch (NumberFormatException nfe) {
            // Test succeeded
        }

        // 3 - Tests error when stored value is non integer and default value is
        assertThat(Cone2ParametersUtils.getIntParam(parameters,
                PARAM_3_KEY,
                Integer.toString(PARAM_3_DEFAULT_VALUE)),
                is(PARAM_3_DEFAULT_VALUE));
    }
}
