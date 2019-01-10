package eu.qedv.tools.ootl.structure;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.model.TestCaseStep;

public class TLTestStep {

	private static final Log log = LogFactory.getLog(TLTestStep.class);
    
    private List<TLTestStep> steps;
    private TLTestStep parent;

    private Duration duration = Duration.ZERO;
    private int count = 1;

    private String name;
    private String info = "";
    private String comment = "";

    private ResultCode result;

    public TLTestStep(TLTestStep parent) {
        this.parent = parent;
        steps = new ArrayList<>();
        if (parent != null) {
            parent.addStep(this);
        }
    }

    public TLTestStep addStep(TLTestStep step) {
        steps.add(step);
        return this;
    }

    public List<TLTestStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public int steps() {
        return steps.size();
    }

    public TLTestStep getStep(int i) {
        return steps.get(i);
    }

    public TLTestStep getParent() {
        return parent;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public TLTestStep setName(String name) {
        this.name = name;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public TLTestStep setInfo(String info) {
        this.info = info;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Duration getDuration() {
        return duration;
    }

    public TLTestStep setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public Date getExecutionTime() {
        if (parent == null) {
            throw new IllegalStateException("Test step has no parent to infer test type from!");
        }
        return parent.getExecutionTime();
    }

    public TLTestStep setResult(ResultCode result) {
        this.result = result;
        return this;
    }

    public ResultCode getResult() {
        if (steps.isEmpty()) {
            return result == null ? ResultCode.NOT_RUN : result;
        }
        ResultCode res = ResultCode.SUCCESS;
        for (TLTestStep step : steps) {
            switch(step.getResult()) {
            case FAILURE:
                return ResultCode.FAILURE;
            case NOT_RUN:
                return ResultCode.NOT_RUN;
            case SKIPPED:
                res = ResultCode.SKIPPED;
                break;
            default:
            }
        }
        return res;
    }

    public ExecutionStatus getStatus() {
        return getResult().toExecutionStatus();
    }

    public void mergeSteps() {
        if (steps.isEmpty()) {
            return;
        }
        for (TLTestStep step : steps) {
            step.mergeSteps();
        }
        int m = 0;
        TLTestStep merging = steps.get(0);
        while (m < steps.size() - 1) {
            TLTestStep next = steps.get(m + 1);
            if (merging.equals(next)) {
                merging.count += next.count;
                merging.duration = merging.duration.plus(next.duration);
                steps.remove(m + 1);
            } else {
                merging = steps.get(++m);
            }
        }
    }
    
    public List<TestCaseStep> toSteps(int version) {
        List<TestCaseStep> testCaseSteps = new ArrayList<>();
        int i = 0;
        for (TLTestStep step : getSteps()) {
            testCaseSteps.add(new TestCaseStep(i, version, i + 1, step.getName() + step.getFormattedCount(" (x", ")"), "", true,
                    ExecutionType.AUTOMATED));
            ++i;
        }
        return testCaseSteps;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TLTestStep)) {
            return false;
        }
        TLTestStep other = (TLTestStep) obj;
        boolean nameEqual = (name == null ? other.name == null : name.equals(other.name));
        return nameEqual && result == other.result && steps.equals(other.steps);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((name == null) ? 0 : name.hashCode());
        hash = prime * hash + ((result == null) ? 0 : result.hashCode());
        hash = prime * hash + ((steps == null) ? 0 : steps.hashCode());
        return hash;
    }

    public String getFormattedCount(String pre, String post) {
        return count == 1 ? "" : pre + count + post;
    }
    
    public String getFormattedDuration() {
        if (duration.getSeconds() == 0)
            return duration.toMillis() + "ms";
        long h = duration.toHours();
        long m = duration.toMinutes() - 60 * h;
        long s = duration.getSeconds() - 60 * (60 * h + m);
        // round seconds
        if (duration.getNano() > 0.5e9) {
            ++s;
        }
        return (h == 0 ? "" : h + "h ") + (h == 0 && m == 0 ? "" : m + "m ") + s + "s";
    }

    @Override
	public String toString() {
        return getClass().getSimpleName() + " [" + getResult() + "]" + ": " + getName() + getFormattedCount(" (x", ")") + ", "
                + getFormattedDuration();
    }

    private void dump(String pre) {
        log.debug(pre + toString());
        String newPre = pre + "  ";
        for (TLTestStep step : steps) {
            step.dump(newPre);
        }
    }

    public void dump() {
        dump("");
    }
}
