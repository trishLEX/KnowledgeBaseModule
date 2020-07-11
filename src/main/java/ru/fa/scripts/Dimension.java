package ru.fa.scripts;

import java.util.Objects;

public class Dimension {

    private int id;
    private String strId;
    private String label;
    private DimensionType dimensionType;
    private DimensionSubType dimensionSubType;
    private Integer parentId;
    private String question;

    public Dimension(
            int id,
            String strId,
            String label,
            DimensionType dimensionType,
            DimensionSubType dimensionSubType,
            Integer parentId,
            String question
    ) {
        this.id = id;
        this.strId = strId;
        this.label = label;
        this.dimensionType = dimensionType;
        this.dimensionSubType = dimensionSubType;
        this.parentId = parentId;
        this.question = question;
    }

    public int getId() {
        return id;
    }

    public String getStrId() {
        return strId;
    }

    public String getLabel() {
        return label;
    }

    public DimensionType getDimensionType() {
        return dimensionType;
    }

    public DimensionSubType getDimensionSubType() {
        return dimensionSubType;
    }

    public Integer getParentId() {
        return parentId;
    }

    public String getQuestion() {
        return question;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dimension dimension = (Dimension) o;
        return id == dimension.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Dimension{" +
                "id=" + id +
                ", strId='" + strId + '\'' +
                ", label='" + label + '\'' +
                ", dimensionType=" + dimensionType +
                ", dimensionSubType=" + dimensionSubType +
                ", parentId=" + parentId +
                ", question='" + question + '\'' +
                '}';
    }
}
