package Component.Statistic.CreateMatrix;

import Component.File.MatrixFile.MatrixFile;
import Component.Statistic.AbstractStat;
import Component.unit.Configure;

import java.io.File;

/**
 * Created by snowf on 2019/4/2.
 */

public class CreateMatrixStat extends AbstractStat {
    public File OutDir;
    public int[] Resolutions;
    public int[] DrawResolutions;
    public Stat[] resolutions;
    public Stat[] draw_resolutions;

    @Override
    public void Stat() {

    }

    @Override
    public String Show() {
        UpDate();
        StringBuilder show = new StringBuilder();
        show.append("=====================================Create matrix Statistic=====================================\n");
        show.append("Output directory:\t").append(OutDir).append("\n");
        show.append("------------------------------------------------------------------------------------------------\n");
        return show.toString();
    }

    @Override
    protected void UpDate() {

    }

    @Override
    public void Init() {
        resolutions = new Stat[Resolutions.length];
        for (int i = 0; i < resolutions.length; i++) {
            resolutions[i] = new Stat();
            resolutions[i].Resolution = Resolutions[i];
            resolutions[i].ChromHeatMapPng = new File[Configure.Chromosome.length];
            resolutions[i].ChromMatrixFile = new MatrixFile[Configure.Chromosome.length];
        }
        draw_resolutions = new Stat[DrawResolutions.length];
        for (int i = 0; i < draw_resolutions.length; i++) {
            draw_resolutions[i] = new Stat();
            draw_resolutions[i].Resolution = DrawResolutions[i];
            draw_resolutions[i].ChromHeatMapPng = new File[Configure.Chromosome.length];
            draw_resolutions[i].ChromMatrixFile = new MatrixFile[Configure.Chromosome.length];
        }
    }
}
