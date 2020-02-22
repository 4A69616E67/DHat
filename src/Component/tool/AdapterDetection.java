package Component.tool;

import Component.File.CommonFile.CommonFile;
import Component.File.FastQFile.FastqFile;
import Component.File.FileTool;
import Component.unit.Opts;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;


/**
 * Created by snowf on 2019/4/26.
 */

public class AdapterDetection {
    public static void main(String[] args) throws ParseException, InterruptedException, IOException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").longOpt("input").hasArg().argName("file").desc("input fastq file").required().build());
        Argument.addOption(Option.builder("p").longOpt("prefix").hasArg().argName("string").desc("out prefix").build());
        Argument.addOption(Option.builder("c").longOpt("cutoff").hasArg().argName("int").desc("cutoff site").build());
        if (args.length < 2) {
            new HelpFormatter().printHelp("java -cp Path/" + Opts.JarFile.getName() + " " + AdapterDetection.class.getName(), Argument, true);
            System.exit(1);
        }
        CommandLine ComLine = new DefaultParser().parse(Argument, args);
        FastqFile InFile = new FastqFile(Opts.GetStringOpt(ComLine, "i", null));
        String Prefix = Opts.GetStringOpt(ComLine, "p", "out");
        int Cutoff = Opts.GetIntOpt(ComLine, "c", 0);
        String Adapter = FileTool.AdapterDetection(InFile, new File(Prefix), Cutoff, new CommonFile(Prefix + ".stat.txt"));
        System.out.println("Detected Adapter Seq: " + Adapter);
    }
}
