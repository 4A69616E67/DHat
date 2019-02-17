package Component.Process;

import org.apache.commons.cli.Options;

import java.util.Date;

/**
 * Created by 浩 on 2019/1/29.
 */
public abstract class AbstractProcess {
    protected String ProcessID;
    protected Date StartTime = new Date(), EndTime = new Date();
    protected int ThreadNum = 1;
    protected Options Argument = new Options();

    public AbstractProcess(String id) {
        ProcessID = id;
    }

    abstract protected void Init()throws Exception;
    abstract protected void ArgumentInit()throws Exception;

    abstract public int run()throws Exception;

    public String getProcessID() {
        return ProcessID;
    }

    public Date getRunTime() {
        return new Date(EndTime.getTime() - StartTime.getTime());
    }

    public void setThreadNum(int threadNum) {
        ThreadNum = threadNum;
    }

    public Date getStartTime() {
        return StartTime;
    }

    public Date getEndTime() {
        return EndTime;
    }
}
