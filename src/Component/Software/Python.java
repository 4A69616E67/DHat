package Component.Software;

import java.io.File;

/**
 * Created by snowf on 2019/3/11.
 */

public class Python extends AbstractSoftware {
    Python(String exe) {
        super(exe);
    }

    @Override
    protected void Init() {

    }

    @Override
    protected String getVersion() {
        return "";
    }

    @Override
    protected File getPath() {
        return null;
    }


}
