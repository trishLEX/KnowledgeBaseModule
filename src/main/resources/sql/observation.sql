create table observation
(
    id bigserial not null
        constraint observation_pk
            primary key,
    str_id varchar(25) not null
);

alter table observation owner to postgres;

create unique index observation_str_id_uindex
    on observation (str_id);

