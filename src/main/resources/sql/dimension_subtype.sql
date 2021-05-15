--liquibase formatted sql

--changeset trishlex:dim-subtype-create-table
create table if not exists dimension_subtype
(
    id serial not null
        constraint dimension_subtype_pk
            primary key,
    subtype varchar(25),
    num integer not null
);

--changeset trishlex:dim-subtype-owner
alter table dimension_subtype owner to postgres;

--changeset trishlex:dim-subtype-num-uindex
create unique index if not exists dimension_subtype_num_uindex
    on dimension_subtype (num);

--changeset trishlex:dim-subtype-subtype-uindex
create unique index if not exists dimension_subtype_subtype_uindex
    on dimension_subtype (subtype);
