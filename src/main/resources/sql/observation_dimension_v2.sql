create table observation_dimension_v2
(
    dimension_id bigint not null
        constraint observation_dimension_dimension_id_fk
            references dimension
            on update cascade on delete cascade,
    obs_dimension_id bigint not null
        constraint observation_dimension_obs_dimension_id_fk
            references dimension
            on update cascade on delete cascade,
    observation_id bigint not null
        constraint observation_dimension_observation_id_fk
            references observation
            on update cascade on delete cascade
);

alter table observation_dimension_v2 owner to postgres;

