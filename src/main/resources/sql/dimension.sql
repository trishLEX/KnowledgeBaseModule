create table dimension
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

alter table dimension owner to postgres;

create unique index dimension_str_id_uindex
    on dimension (str_id);

create index dimension_broader_index
    on dimension (broader);

