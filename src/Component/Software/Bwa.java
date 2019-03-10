package Component.Software;

import java.io.File;

/**
 * Created by snowf on 2019/3/10.
 */

public class Bwa extends AbstractSoftware {
    Bwa(String exe) {
        super(exe);
    }

    @Override
    protected float getVersion() {
        return 0;
    }

    @Override
    protected File getPath() {
        return new File(Execution);
    }
}
