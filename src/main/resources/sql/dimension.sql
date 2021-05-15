--liquibase formatted sql

--changeset trishlex:dimension-create-table
create table if not exists dimension
(
    id bigserial not null
        constraint dimension_pk
            primary key,
    str_id varchar(50) not null,
    label varchar(128),
    broader bigint
        constraint dimension_dimension_id_fk
            references dimension
            on update cascade on delete cascade,
    type varchar(25) not null,
    subtype varchar(25) not null,
    question varchar(1024),
    level integer not null,
    all_narrower bigint[],
    narrower bigint[] default '{}'::bigint[] not null
);

--changeset trishlex:dimension-owner
alter table dimension owner to postgres;

--changeset trishlex:dimension-create-uindex
create unique index if not exists dimension_str_id_uindex
    on dimension (str_id);

--changeset trishlex:dimension-create-dimension-broader-index
create index if not exists dimension_broader_index
    on dimension (broader);

--changeset trishlex:dimension-create-dim-subtype-index
create index if not exists dimension_subtype_index on dimension (subtype);

--changeset trishlex:dimension-create-dim-all-narrower-index
create index if not exists dimension_all_narrower_index
    on dimension (cardinality(all_narrower));

