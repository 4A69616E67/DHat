package Component.Software;

import Component.File.CommonFile.CommonFile;
import Component.SystemDhat.CommandLineDhat;
import Component.unit.Configure;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

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
            getPath();
            getVersion();
        }
    }

    @Override
    protected String getVersion() {
//        CommonFile temporaryFile = new CommonFile(Configure.OutPath + "/python.version.tmp");
        try {
            StringWriter buffer = new StringWriter();
            CommandLineDhat.run(Execution + " -V", null, new PrintWriter(buffer));
//            ArrayList<char[]> lines = temporaryFile.Read();
            Version = buffer.toString().split("\\n")[0].split("\\s+")[1];
        } catch (IOException | InterruptedException | IndexOutOfBoundsException e) {
            Valid = false;
        }
//        temporaryFile.delete();
        return Version;
    }

    public boolean installPackage(String packageName) {
        String commandLine = Path + "/Scripts/pip install " + packageName;
        return true;
    }

    public String getPackageVersion(String packageName) {
        String version = null;
        String commandLine = Path + "/Scripts/pip list";
//        CommonFile temporaryFile = new CommonFile(Configure.OutPath + "/python.package_list.tmp");
        try {
            StringWriter buffer = new StringWriter();
            CommandLineDhat.run(commandLine, new PrintWriter(buffer), null);
//            ArrayList<char[]> lines = temporaryFile.Read();
//            temporaryFile.delete();
            for (String c : buffer.toString().split("\\n")) {
                String[] p = c.split("\\s+");
                if (p[0].equals(packageName)) {
                    version = p[1].replaceAll("\\(|\\)", "");
                }
            }
        } catch (IOException | InterruptedException e) {
//            temporaryFile.delete();
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
