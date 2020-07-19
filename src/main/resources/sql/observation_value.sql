create table observation_value
(
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

alter table observation_value owner to postgres;

create index observation_value_value_id_index
    on observation_value (value_id);

create unique index observation_value_observation_id_value_id_value_subtype_uindex
    on observation_value (observation_id, value_id, value_subtype);

