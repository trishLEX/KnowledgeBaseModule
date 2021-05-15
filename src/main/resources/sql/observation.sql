--liquibase formatted sql

--changeset trishlex:observation-create-table
create table if not exists observation
(
    id bigserial not null
        constraint observation_pk
            primary key,
    str_id varchar(25) not null
);

--changeset trishlex:observation-owner
alter table observation owner to postgres;

--changeset trishlex:observation-str-id-uindex
create unique index if not exists observation_str_id_uindex
    on observation (str_id);

