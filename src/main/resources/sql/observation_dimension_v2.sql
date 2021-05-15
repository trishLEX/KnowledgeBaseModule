--liquibase formatted sql

--changeset trishlex:observation-dimension-create-table
create table if not exists observation_dimension_v2
(
    dimension_id bigint not null
        constraint observation_dimension_dimension_id_fk
            references dimension
            on update cascade on delete cascade,
    obs_dimension_id bigint not null
        constraint observation_dimension_obs_dimension_id_fk
            references dimension
            on update cascade on delete cascade,
    observation_id bigint not null
        constraint observation_dimension_observation_id_fk
            references observation
            on update cascade on delete cascade,
    dimension_subtype varchar(25) not null
);

--changeset trishlex:observation-dimension-owner
alter table observation_dimension_v2 owner to postgres;

--changeset trishlex:observation-dimension-oid-dimsubtype-index
create index if not exists observation_dimension_v2_observation_id_dim_subtype_index
    on observation_dimension_v2 (observation_id, dimension_subtype);

--changeset trishlex:observation-dimension-dim-subtype-index
create index if not exists observation_dimension_v2_dim_subtype_index on observation_dimension_v2 (dimension_subtype);
