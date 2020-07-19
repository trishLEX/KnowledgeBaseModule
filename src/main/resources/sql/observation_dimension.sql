create table observation_dimension
(
    dimension_id bigint not null
        constraint observation_dimension_dimension_id_fk
            references dimension
            on update cascade on delete cascade,
    observation_id bigint not null
        constraint observation_dimension_observation_id_fk
            references observation
            on update cascade on delete cascade
);

alter table observation_dimension owner to postgres;

