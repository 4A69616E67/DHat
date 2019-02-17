import java.io.IOException;
import Component.tool.Statistic;
/**
 * Created by snowf on 2019/2/17.
 *
 */
public class FindRestrictionSite {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java -cp DLO-HIC-AnalysisTools.jar FindRestrictionSite <fasta file> <restriction seq> <out prefix>");
            System.exit(0);
        } else {
//            Routine r = new Routine();
            Statistic.FindRestrictionSite(args[0], args[1], args[2]);
        }
    }
}
