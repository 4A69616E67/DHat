package Component.Statistic.Chart;

import java.io.File;
import java.io.IOException;

/**
 * Created by snowf on 2019/11/19.
 */

public interface ChartInterface {
    void loadData(File inputFile) throws IOException;

    void drawing(File outputFile) throws IOException;
}
