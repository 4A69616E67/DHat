package Component.Software;

import Component.File.CommonFile;
import Component.tool.Tools;
import Component.unit.Configure;

import java.io.IOException;
import java.io.PrintWriter;
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
        getPath();
        getVersion();
    }

    @Override
    protected String getVersion() {
        CommonFile temporaryFile = new CommonFile(Configure.OutPath + "/python.version.tmp");
        try {
            Tools.ExecuteCommandStr(Execution + " -V", null, new PrintWriter(temporaryFile));
            ArrayList<char[]> lines = temporaryFile.Read();
            Version = String.valueOf(lines.get(0)).split("\\s+")[1];
        } catch (IOException | InterruptedException | IndexOutOfBoundsException e) {
            Valid = false;
        }
        temporaryFile.delete();
        return Version;
    }

    public boolean installPackage(String packageName) {
        String commandLine = Path + "/pip install " + packageName;
        return true;
    }

    public boolean checkPackage(String packageName) {
        String commandLine = Path + "/pip list";
        CommonFile temporaryFile = new CommonFile(Configure.OutPath + "/python.package_list.tmp");
        try {
            Tools.ExecuteCommandStr(commandLine, new PrintWriter(temporaryFile), null);
            ArrayList<char[]> lines = temporaryFile.Read();
            temporaryFile.delete();
            for (char[] c : lines) {
                String p = String.valueOf(c).split("\\s+")[0];
                if (p.equals(packageName)) {
                    return true;
                }
            }
        } catch (IOException | InterruptedException e) {
            temporaryFile.delete();
            System.err.println("Warning! can't check python package: " + packageName);
        }
        return false;
    }
}
