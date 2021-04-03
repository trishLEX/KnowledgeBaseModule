package ru.fa.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@ParametersAreNonnullByDefault
@JsonDeserialize(builder = Dimension.Builder.class)
public class Dimension implements Comparable<Dimension> {

    @JsonProperty("id")
    private long id;

    @JsonProperty("level")
    private int level;

    @JsonProperty("strId")
    private String strId;

    @JsonProperty("label")
    private String label;

    @JsonProperty("dimensionType")
    private String dimensionType;

    @JsonProperty("dimensionSubType")
    private String dimensionSubType;

    @JsonProperty("parentId")
    private Long parentId;

    @JsonProperty("childrenIds")
    private Set<Long> childrenIds;

    @JsonProperty("allChildrenIds")
    private Set<Long> allChildrenIds;

    @JsonProperty("question")
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

    public String getDimensionType() {
        return dimensionType;
    }

    public String getDimensionSubType() {
        return dimensionSubType;
    }

    public Long getParentId() {
        return parentId;
    }

    public Set<Long> getChildrenIds() {
        return childrenIds;
    }

    public Set<Long> getAllChildrenIds() {
        return allChildrenIds;
    }

    public String getQuestion() {
        return question;
    }

    public void addChildId(long dimensionId) {
        childrenIds.add(dimensionId);
    }

    public void addAllChildrenIds(Collection<Long> ids) {
        allChildrenIds.addAll(ids);
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
    public int compareTo(Dimension o) {
        if (!dimensionSubType.equals(o.dimensionSubType)) {
            throw new IllegalArgumentException("Different subtypes: " + this + " and " + o);
        }

        return -Integer.compare(level, o.level);
    }

    @Override
    public String toString() {
        return "Dimension{" +
                "id=" + id +
                ", level=" + level +
                ", strId='" + strId + '\'' +
                ", label='" + label + '\'' +
                ", dimensionType=" + dimensionType +
                ", dimensionSubType=" + dimensionSubType +
                ", parentId=" + parentId +
                ", childrenIds=" + childrenIds +
                ", allChildrenIds=" + allChildrenIds +
                ", question='" + question + '\'' +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class Builder {
        private long id;
        private int level;
        private String strId;
        private String label;
        private String dimensionType;
        private String dimensionSubType;
        private Long parentId;
        private Set<Long> childrenIds = new HashSet<>();
        private Set<Long> allChildrenIds = new HashSet<>();
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

        public Builder setDimensionType(String dimensionType) {
            this.dimensionType = dimensionType;
            return this;
        }

        public Builder setDimensionSubType(String dimensionSubType) {
            this.dimensionSubType = dimensionSubType;
            return this;
        }

        public Builder setParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder setChildrenIds(Set<Long> childrenIds) {
            this.childrenIds = childrenIds;
            return this;
        }

        public Builder setAllChildrenIds(Set<Long> allChildrenIds) {
            this.allChildrenIds = allChildrenIds;
            return this;
        }

        public Dimension.Builder setQuestion(String question) {
            this.question = question;
            return this;
        }

        public Dimension build() {
            return new Dimension(this);
        }
    }
}
