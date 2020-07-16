package ru.fa.scripts;

import java.util.Objects;

public class Dimension {

    private long id;
    private int level;
    private String strId;
    private String label;
    private DimensionType dimensionType;
    private DimensionSubType dimensionSubType;
    private Long parentId;
    private String question;

    public Dimension(
            long id,
            int level,
            String strId,
            String label,
            DimensionType dimensionType,
            DimensionSubType dimensionSubType,
            Long parentId,
            String question
    ) {
        this.id = id;
        this.level = level;
        this.strId = strId;
        this.label = label;
        this.dimensionType = dimensionType;
        this.dimensionSubType = dimensionSubType;
        this.parentId = parentId;
        this.question = question;
    }

    public long getId() {
        return id;
    }

    public int getLevel() {
        return level;
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

    public Long getParentId() {
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
