package ru.fa.util;

import ru.fa.model.Dimension;

import java.util.List;

public class DimensionsUtil {

    public static void compareDimensions(
            List<Dimension> upper, List<Dimension> equals, List<Dimension> lower,
            Dimension dimension, Dimension anotherDimension
    ) {
        switch (dimension.compareTo(anotherDimension)) {
            case -1:
                lower.add(dimension);
                break;
            case 0:
                equals.add(dimension);
                break;
            case 1:
                upper.add(dimension);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public static boolean isOneBranch(Dimension dimension, Dimension anotherDimension) {
        if (!dimension.getDimensionSubType().equals(anotherDimension.getDimensionSubType())) {
            throw new IllegalArgumentException("Different dimension subtypes");
        } else if (dimension.equals(anotherDimension)) {
            return true;
        } else if (dimension.getLevel() == anotherDimension.getLevel()) {
            return false;
        }

        return dimension.getAllChildrenIds().contains(anotherDimension.getId())
                || anotherDimension.getAllChildrenIds().contains(dimension.getId());
    }
}
