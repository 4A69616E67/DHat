package Utils;

import Component.File.CommonFile;
import Component.File.FastQFile.FastqFile;
import Component.File.FastQFile.FastqItem;
import Component.unit.Opts;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * Created by snowf on 2019/3/4.
 */

public class FastqExtract {
    public static void main(String[] args) throws IOException {
        Options Argument = new Options();
        Argument.addOption(Option.builder("i").hasArg().argName("file").desc("input file").required().build());//输入文件
        Argument.addOption(Option.builder("list").hasArg().argName("file").desc("read id file").build());//配置文件
        Argument.addOption(Option.builder("n").hasArg().argName("int").desc("item number").build());//配置文件
        Argument.addOption(Option.builder("t").hasArg().argName("int").desc("thread").build());//配置文件
        Argument.addOption(Option.builder("f").hasArg().argName("file").desc("out file").build());//配置文件
        final String helpFooter = "Note: use \"java -jar " + Opts.JarFile.getName() + " install\" when you first use!\n      JVM can get " + String.format("%.2f", Opts.MaxMemory / Math.pow(10, 9)) + "G memory";
        if (args.length == 0) {
            //没有参数时打印帮助信息
            new HelpFormatter().printHelp("java -jar Path/" + Opts.JarFile.getName(), "", Argument, helpFooter, true);
            System.exit(1);
        }
        CommandLine ComLine = null;
        try {
            ComLine = new DefaultParser().parse(Argument, args);
        } catch (ParseException e) {
            //缺少参数时打印帮助信息
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp("java -jar Path/" + Opts.JarFile.getName(), "", Argument, helpFooter, true);
            System.exit(1);
        }
        FastqFile inputFile = new FastqFile(Opts.GetStringOpt(ComLine, "i", null));
        FastqFile outputFile = ComLine.hasOption("f") ? new FastqFile(Opts.GetFileOpt(ComLine, "f", null)) : new FastqFile(inputFile.getPath() + ".out");
        CommonFile listfile = ComLine.hasOption("list") ? new CommonFile(Opts.GetStringOpt(ComLine, "list", null)) : null;
        int LineNum = Opts.GetIntOpt(ComLine, "n", 0);
        int threads = Opts.GetIntOpt(ComLine, "t", 1);
        FastqFile TempFile;
        HashSet<String> IDList = new HashSet<>();
        String[] line;
        if (listfile != null) {
            listfile.ReadOpen();
            while ((line = listfile.ReadItemLine()) != null) {
                IDList.add(line[0]);
            }
            listfile.ReadClose();
        }
        if (LineNum <= 0) {
            TempFile = inputFile;
        } else {
            TempFile = new FastqFile(inputFile.getPath() + ".temp");
            BufferedWriter writer = TempFile.WriteOpen();
            inputFile.ReadOpen();
            int count = 1;
            while ((line = inputFile.ReadItemLine()) != null && count <= LineNum) {
                writer.write(String.join("\n", line) + "\n");
                count++;
            }
            writer.close();
            inputFile.ReadClose();
        }
        if (listfile == null) {
            FileUtils.moveFile(TempFile, outputFile);
        } else {
            outputFile.WriteOpen();
            for (FastqItem i : TempFile.ExtractID(IDList, threads)) {
                outputFile.WriteItemln(i);
            }
            outputFile.WriteClose();
            if (LineNum > 0) {
                TempFile.delete();
            }
        }

    }
}
