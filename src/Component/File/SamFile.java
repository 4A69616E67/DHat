package Component.File;

import Component.unit.SamItem;
import Component.unit.SortItem;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by snowf on 2019/2/17.
 */

public class SamFile extends AbstractFile<SamItem> {
    private ArrayList<String> Header = new ArrayList<>();
    private boolean SortByName = true;

    public SamFile(String pathname) {
        super(pathname);
    }

//    public SamFile(SamFile file) {
//        super(file);
//    }

    @Override
    protected SamItem ExtractItem(String[] s) {
        if (s == null) {
            return null;
        }
        Item = new SamItem();
        if (s[0].matches("^@.*")) {
            Header.add(s[0]);
        } else {
            String[] line_split = s[0].split("\\s+");
            Item.Title = line_split[0];
            Item.Stat = Integer.parseInt(line_split[1]);
            Item.Chr = line_split[2];
            Item.BeginSite = Integer.parseInt(line_split[3]);
            Item.MappingQuality = Integer.parseInt(line_split[4]);
            Item.MappingStat = line_split[5];
            Item.Sequence = line_split[9];
            Item.Quality = line_split[10];
            Item.SortByName = SortByName;
        }
        return Item;
    }

    @Override
    public void WriteItem(SamItem item) throws IOException {
        writer.write(item.toString());
    }

    @Override
    protected SortItem<SamItem> ExtractSortItem(String[] s) {
        return null;
    }


    private static int CalculateFragLength(String s) {
        int Length = 0;
        StringBuilder Str = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case 'M':
                case 'D':
                case 'N':
                    Length += Integer.parseInt(Str.toString());
                    Str.setLength(0);
                    break;
                case 'I':
                case 'S':
                case 'P':
                case 'H':
                    Str.setLength(0);
                    break;
                default:
                    Str.append(s.charAt(i));
            }
        }
        return Length;
    }

    public void ToBedFile(BedFile bedFile) throws IOException {
        System.out.println(new Date() + "\tBegin\t" + getName() + " to " + bedFile.getName());
        ReadOpen();
        BufferedReader reader = getReader();
        BufferedWriter writer = bedFile.WriteOpen();
        String Line;
        String[] Str;
        String Orientation;
        while ((Line = reader.readLine()) != null) {
            if (Line.matches("^@.*")) {
                continue;
            }
            Str = Line.split("\\s+");
            Orientation = (Integer.parseInt(Str[1]) & 16) == 16 ? "-" : "+";
            writer.write(Str[2] + "\t" + Str[3] + "\t" + (Integer.parseInt(Str[3]) + CalculateFragLength(Str[5]) - 1) + "\t" + Str[0] + "\t" + Str[4] + "\t" + Orientation + "\n");
        }
        writer.close();
        reader.close();
    }

    public ArrayList<String> getHeader() {
        return Header;
    }
}

