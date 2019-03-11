package Component.Software;

import java.io.File;

/**
 * Created by snowf on 2019/3/10.
 */

public abstract class AbstractSoftware {
    protected File Path;
    protected String Version;
    protected String Execution;
    protected boolean Valid = false;

    AbstractSoftware(String exe) {
        Execution = exe;
        Init();
    }

    protected abstract void Init();

    protected abstract String getVersion();

    protected abstract File getPath();

    public String version() {
        return Version != null ? Version : getVersion();
    }

    public boolean isValid() {
        return Valid;
    }
}
