package Component.Software;

import Component.SystemDhat.CommandLineDhat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by snowf on 2019/6/14.
 */

public class MAFFT extends AbstractSoftware {
    public MAFFT(String exe) {
        super(exe);
    }

    @Override
    protected void Init() {
        if (Execution.trim().equals("")) {
            System.err.println("[mafft]\tNo execute file");
        } else {
            if (Path.getName().equals("")) {
                getPath();
            }
            getVersion();
        }
    }

    @Override
    protected String getVersion() {
        try {
            StringWriter buffer = new StringWriter();
            CommandLineDhat.run(Execution + " --version", null, new PrintWriter(buffer));
            Version = buffer.toString().split("\\n")[0];
        } catch (IOException | InterruptedException e) {
            Valid = false;
        }
        return Version;
    }
}
