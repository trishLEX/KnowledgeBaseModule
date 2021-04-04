package ru.fa.scripts.model;

import java.util.List;
import java.util.Objects;

public class Dimension {

    private long id;
    private int level;
    private String strId;
    private String label;
    private DimensionType dimensionType;
    private DimensionSubType dimensionSubType;
    private Long parentId;
    private List<Long> childrenIds;
    private List<Long> allChildrenIds;
    private String question;

    private Dimension(Builder builder) {
        this.id = builder.id;
        this.level = builder.level;
        this.strId = builder.strId;
        this.label = builder.label;
        this.dimensionType = builder.dimensionType;
        this.dimensionSubType = builder.dimensionSubType;
        this.parentId = builder.parentId;
        this.childrenIds = builder.childrenIds;
        this.allChildrenIds = builder.allChildrenIds;
        this.question = builder.question;
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

    public List<Long> getChildrenIds() {
        return childrenIds;
    }

    public Dimension setChildrenIds(List<Long> childrenIds) {
        this.childrenIds = childrenIds;
        return this;
    }

    public List<Long> getAllChildrenIds() {
        return allChildrenIds;
    }

    public Dimension setAllChildrenIds(List<Long> allChildrenIds) {
        this.allChildrenIds = allChildrenIds;
        return this;
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

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private long id;
        private int level;
        private String strId;
        private String label;
        private DimensionType dimensionType;
        private DimensionSubType dimensionSubType;
        private Long parentId;
        private List<Long> childrenIds;
        private List<Long> allChildrenIds;
        private String question;

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setLevel(int level) {
            this.level = level;
            return this;
        }

        public Builder setStrId(String strId) {
            this.strId = strId;
            return this;
        }

        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder setDimensionType(DimensionType dimensionType) {
            this.dimensionType = dimensionType;
            return this;
        }

        public Builder setDimensionSubType(DimensionSubType dimensionSubType) {
            this.dimensionSubType = dimensionSubType;
            return this;
        }

        public Builder setParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder setChildrenIds(List<Long> childrenIds) {
            this.childrenIds = childrenIds;
            return this;
        }

        public Builder setAllChildrenIds(List<Long> allChildrenIds) {
            this.allChildrenIds = allChildrenIds;
            return this;
        }

        public Builder setQuestion(String question) {
            this.question = question;
            return this;
        }

        public Dimension build() {
            return new Dimension(this);
        }
    }
}
