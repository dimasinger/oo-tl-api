package de.blinklan.tools.ootl.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class TLTestResult extends TLTestStep {

    private Date executionTime;

    private List<String> testSuites;

    public TLTestResult() {
        super(null);
        testSuites = new ArrayList<>();
    }

    @Override
    public Date getExecutionTime() {
        if(executionTime==null) {
            return null;
        }
        return new Date(executionTime.getTime());
    }

    public TLTestResult setExecutionTime(Date executionTime) {
        this.executionTime = new Date(executionTime.getTime());
        return this;
    }

    public List<String> getTestSuites() {
        return Collections.unmodifiableList(testSuites);
    }

    public String getTestSuite() {
        return testSuites.stream().reduce(null, (s, suite) -> (s == null ? suite : s + "." + suite));
    }

    public TLTestResult setTestSuites(List<String> testSuites) {
        this.testSuites = new ArrayList<>();
        this.testSuites.addAll(testSuites);
        return this;
    }
}
