package Component.Software;

import Component.SystemDhat.CommandLineDhat;
import Component.unit.Opts;

import java.io.*;

/**
 * Created by snowf on 2019/3/10.
 */

public abstract class AbstractSoftware {
    protected File Path = new File("");
    protected String Version = "";
    protected String Execution;
    protected boolean Valid = false;

    AbstractSoftware(String exe) {
        Execution = exe;
        if (new File(exe).isFile()) {
            Path = new File(new File(exe).getParent());
            Execution = new File(exe).getName();
        }
        Init();
    }

    protected abstract void Init();

    protected abstract String getVersion();

    protected File FindPath() {
        try {
            String ComLine;
            if (Opts.OsName.matches(".*(?i)windows.*")) {
                ComLine = "where " + Execution;
            } else {
                ComLine = "which " + Execution;
            }
            StringWriter buffer = new StringWriter();
            new CommandLineDhat().run(ComLine, new PrintWriter(buffer), null);
            Path = new File(buffer.toString().split("\\n")[0]).getParentFile();
//            Execution = Path + "/" + Execution;
            Valid = true;
        } catch (IOException | InterruptedException | IndexOutOfBoundsException e) {
            System.err.println("Warning! can't locate " + Execution + " full path");
            System.err.println("Please check the name of execute file or set absolute path in configure file");
//            System.exit(1);
        }
        return Path;
    }

    public String version() {
        return Version != null ? Version : getVersion();
    }

    public boolean isValid() {
        return Valid;
    }

    @Override
    public String toString() {
        return Execution + "\tVersion: " + Version;
    }

    public String Exe() {
        return Execution;
    }

    public File Path() {
        return Path;
    }

    public File FullExe() {
        return new File((Path == null ? "" : Path + "/") + Execution);
    }
}
