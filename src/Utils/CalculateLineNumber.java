package Utils;

import java.util.Date;

import Component.File.AbstractFile;
import Component.File.CommonFile.CommonFile;
import Component.File.FastQFile.FastqFile;
import Component.File.FastaFile.FastaFile;
import Component.File.SamFile.SamFile;
import Component.tool.Tools;
import Component.unit.Opts;
import org.apache.commons.cli.*;

/**
 * Created by snowf on 2019/2/17.
 */
public class CalculateLineNumber {
    public static void main(String[] args) throws ParseException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("f").hasArgs().desc("files").required().build());
        Argument.addOption(Option.builder("k").hasArg().desc("file type (fa, fq, sam)").build());
        if (args.length < 1) {
            new HelpFormatter().printHelp("java -cp " + Opts.JarFile.getName() + " " + CalculateLineNumber.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        String[] InputFileNames = Opts.GetStringOpts(ComLine, "f", null);
        String FileType = Opts.GetStringOpt(ComLine, "k", "");
        AbstractFile[] InputFiles = null;
        switch (FileType) {
            case "fa":
                InputFiles = new FastaFile[InputFileNames.length];
                for (int i = 0; i < InputFileNames.length; i++) {
                    InputFiles[i] = new FastaFile(InputFileNames[i]);
                }
                break;
            case "fq":
                InputFiles = new FastqFile[InputFileNames.length];
                for (int i = 0; i < InputFileNames.length; i++) {
                    InputFiles[i] = new FastqFile(InputFileNames[i]);
                }
                break;
            case "sam":
                InputFiles = new SamFile[InputFileNames.length];
                for (int i = 0; i < InputFileNames.length; i++) {
                    InputFiles[i] = new SamFile(InputFileNames[i]);
                }
                break;
            default:
                InputFiles = new CommonFile[InputFileNames.length];
                for (int i = 0; i < InputFileNames.length; i++) {
                    InputFiles[i] = new CommonFile(InputFileNames[i]);
                }
        }
        final AbstractFile[] finalInputFiles = InputFiles;
        Thread[] t = new Thread[InputFiles.length];
        for (int i = 0; i < t.length; i++) {
            int finalI = i;
            t[i] = new Thread(() -> {
                long linenumber = finalInputFiles[finalI].getItemNum();
                synchronized (Thread.class) {
                    System.out.println(new Date() + "\t" + finalInputFiles[finalI] + " item number is:\t" + linenumber);
                }
            });
            t[i].start();
        }
        Tools.ThreadsWait(t);
    }
}
