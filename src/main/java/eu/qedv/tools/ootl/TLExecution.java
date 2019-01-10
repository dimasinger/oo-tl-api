package eu.qedv.tools.ootl;

import br.eti.kinoshita.testlinkjavaapi.model.Execution;
import eu.qedv.tools.ootl.structure.ResultCode;

/**
 * Wraps the execution result of a TLTestCase in a TLBuild
 * 
 * @author dimasinger
 *
 */
public class TLExecution {
	protected final TestLink tl;
    
    protected final TLBuild build;
    protected final TLTestCase testcase;
    
    protected final Execution execution;
    
    protected TLExecution(TestLink tl, TLBuild build, TLTestCase testcase, Execution execution) {
        this.tl = tl;
        this.build = build;
        this.testcase = testcase;
        this.execution = execution;
    }
    
    /*
     * Getters
     */
    
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
