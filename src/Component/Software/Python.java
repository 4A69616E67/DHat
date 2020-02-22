package Component.Software;

import Component.SystemDhat.CommandLineDhat;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by snowf on 2019/3/11.
 */

public class Python extends AbstractSoftware {
    public Python(String exe) {
        super(exe);
    }

    @Override
    protected void Init() {
        if (Execution.trim().equals("")) {
            System.err.println("[python]\tNo execute file");
        } else {
            if (!Path.isDirectory()) {
                FindPath();
            }
            getVersion();
        }
    }

    @Override
    protected String getVersion() {
        try {
            StringWriter buffer = new StringWriter();
            CommandLineDhat.run(FullExe() + " -V", null, new PrintWriter(buffer));
            Version = buffer.toString().split("\\n")[0].split("\\s+")[1];
        } catch (IOException | InterruptedException | IndexOutOfBoundsException e) {
            Valid = false;
        }
        return Version;
    }

    public boolean installPackage(String packageName) {
        String commandLine = Path + "/Scripts/pip install " + packageName;
        return true;
    }

    public String getPackageVersion(String packageName) {
        String version = null;
        String commandLine = Path + "/Scripts/pip list";
        try {
            StringWriter buffer = new StringWriter();
            CommandLineDhat.run(commandLine, new PrintWriter(buffer), null);
            for (String c : buffer.toString().split("\\n")) {
                String[] p = c.split("\\s+");
                if (p[0].equals(packageName)) {
                    version = p[1].replaceAll("\\(|\\)", "");
                }
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Warning! can't get python package: " + packageName);
        }
        return version;
    }


    public boolean checkPackage(String packageName, String version) {
        if (version == null || version.trim().equals("")) {
            return true;
        }
        String pversion = getPackageVersion(packageName);
        return pversion != null && pversion.equals(version);
    }
}
