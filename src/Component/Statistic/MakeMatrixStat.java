package Component.Statistic;

import java.io.File;

/**
 * Created by snowf on 2019/4/2.
 */

public class MakeMatrixStat extends AbstractStat {
    public File OutDir;
    public int[] Resolutions;

    @Override
    public void Stat() {

    }

    @Override
    public String Show() {
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("##==========================Make matrix===========================##\n");
        show.append("Output directory:\t").append(OutDir).append("\n");
        show.append("--------------------------------------------------------------------\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {

    }

    @Override
    public void Init() {

    }
}
