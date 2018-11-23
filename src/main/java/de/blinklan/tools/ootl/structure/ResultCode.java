package de.blinklan.tools.ootl.structure;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;

/**
 * Enum representing the status of a test execution
 * @author dimasinger
 *
 */
public enum ResultCode {
    NOT_RUN, SUCCESS, SKIPPED, FAILURE;
    
    public static ResultCode byStatusCode(int status) {
        switch (status) {
        case 0:
            return SKIPPED;
        case 1:
            return SUCCESS;
        default:
            return FAILURE;
        }
    }
    
    public static ResultCode fromExecutionStatus(ExecutionStatus status) {
        switch(status) {
        case PASSED:
            return SUCCESS;
        case BLOCKED:
            return SKIPPED;
        case NOT_RUN:
            return NOT_RUN;
        default:
            return FAILURE;
        }
    }

    public ExecutionStatus toExecutionStatus() {
        switch (this) {
        case SUCCESS:
            return ExecutionStatus.PASSED;
        case SKIPPED:
            return ExecutionStatus.BLOCKED;
        case NOT_RUN:
            return ExecutionStatus.NOT_RUN;
        default:
            return ExecutionStatus.FAILED;
        }
    }
}
