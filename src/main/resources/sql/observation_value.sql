--liquibase formatted sql

--changeset trishlex:observation-value-create-table
create table if not exists observation_value
(
    id bigserial primary key not null,
    observation_id bigint
        constraint observation_value_observation_id_fk
            references observation
            on update cascade on delete cascade,
    value_id bigint
        constraint observation_value_value_id_fk
            references value
            on update cascade on delete cascade,
    value_subtype varchar(50) not null
);

--changeset trishlex:observation-value-owner
alter table observation_value owner to postgres;

--changeset trishlex:observation-value-value-id-index
create index if not exists observation_value_value_id_index
    on observation_value (value_id);

--changeset trishlex:observation-value-uindex
create unique index if not exists observation_value_observation_id_value_id_value_subtype_uindex
    on observation_value (observation_id, value_id, value_subtype);

