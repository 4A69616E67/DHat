package Component.tool;

import Component.unit.InterAction;

import java.util.ArrayList;

/**
 * Created by snowf on 2019/2/17.
 *
 */

public class BedPeFilter {
    private ArrayList<InterAction> FilterList;

    public BedPeFilter(ArrayList<InterAction> filterlist) {
        FilterList = filterlist;
    }

    public Boolean Run(InterAction action) {
        for (int j = 0; j < FilterList.size(); j++) {
            if (action.getLeft().Chr.Name.equals(FilterList.get(j).getLeft().Chr.Name) && action.getRight().Chr.Name.equals(FilterList.get(j).getRight().Chr.Name)) {
                if (action.IsBelong(FilterList.get(j))) {
                    FilterList.get(j).Count--;
                    if (FilterList.get(j).Count == 0) {
                        FilterList.remove(j);
                    }
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

}
