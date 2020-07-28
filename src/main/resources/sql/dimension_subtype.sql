create table dimension_subtype
(
    id serial not null
        constraint dimension_subtype_pk
            primary key,
    subtype varchar(25),
    num integer not null
);

alter table dimension_subtype owner to postgres;

create unique index dimension_subtype_num_uindex
    on dimension_subtype (num);

create unique index dimension_subtype_subtype_uindex
    on dimension_subtype (subtype);
