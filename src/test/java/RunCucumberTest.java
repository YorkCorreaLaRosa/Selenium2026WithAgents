import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "step")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:")
//@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@Kiwi")
public class RunCucumberTest {
}
