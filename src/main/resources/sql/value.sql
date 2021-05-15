--liquibase formatted sql

--changeset trishlex:value-create-table
create table if not exists value
(
    id bigserial not null
        constraint value_pk
            primary key,
    str_id varchar(25) not null,
    content jsonb,
    type varchar(25) not null
);

--changeset trishlex:value-owner
alter table value owner to postgres;

--changeset trishlex:value-str-id-type-uindex
create unique index if not exists value_str_id_type_uindex
    on value (str_id, type);

