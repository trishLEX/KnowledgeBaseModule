create table value
(
    id bigserial not null
        constraint value_pk
            primary key,
    str_id varchar(25) not null,
    content jsonb,
    type varchar(25) not null
);

alter table value owner to postgres;

create unique index value_str_id_type_uindex
    on value (str_id, type);

