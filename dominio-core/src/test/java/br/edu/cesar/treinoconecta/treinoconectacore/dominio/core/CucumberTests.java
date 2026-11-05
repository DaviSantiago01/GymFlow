package br.edu.cesar.treinoconecta.treinoconectacore.dominio.core;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.Suite;

import io.cucumber.junit.platform.engine.Constants;

@Suite(failIfNoTests = false)
@IncludeEngines("cucumber")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "br.edu.cesar.treinoconecta.treinoconectacore.dominio.core")
@SelectDirectories("src/test/resources")
public class CucumberTests {
}
