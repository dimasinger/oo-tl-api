package de.blinklan.tools.ootl;

import de.blinklan.tools.ootl.structure.ResultCode;

import br.eti.kinoshita.testlinkjavaapi.model.Execution;

/**
 * Wraps the execution result of a TLTestCase in a TLBuild
 * 
 * @author dvo
 *
 */
public class TLExecution {
    final TestLink tl;
    
    final TLBuild build;
    final TLTestCase testcase;
    
    final Execution execution;
    
    TLExecution(TestLink tl, TLBuild build, TLTestCase testcase, Execution execution) {
        this.tl = tl;
        this.build = build;
        this.testcase = testcase;
        this.execution = execution;
    }
    
    /*
     * Getters
     */
    
    public boolean notExecuted() {
        return execution == null;
    }
    
    public TLTestCase getTestcase() {
        return testcase;
    }
    
    public TLBuild getBuild() {
        return build;
    }
    
    public ResultCode getExecutionResult() {
        return ResultCode.fromExecutionStatus(execution.getStatus());
    }
    
    public String getExecutionNotes() {
        return execution.getNotes();
    }
}
