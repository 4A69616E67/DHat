package Component.Software;

import java.io.File;

/**
 * Created by snowf on 2019/3/10.
 */

public abstract class AbstractSoftware {
    protected File Path;
    protected float Version;
    protected String Execution;

    AbstractSoftware(String exe) {
        Execution = exe;
    }


    protected abstract float getVersion();

    protected abstract File getPath();

    public float version() {
        return Version;
    }
}
