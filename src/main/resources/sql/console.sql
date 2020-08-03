create type dimension_type as enum (
    'BANKING_PRODUCT'
    );

create type dimension_subtype as enum (
    'CARD_CATEGORY',
    'CARD_TYPE',
    'CARD_PRODUCT',
    'CARD_PAYMENT_SYSTEM',
    'CARD_SPECIFIC',
    'CARD_CURRENCY',
    'CARD_LEVEL',
    'CARD_BANK',
    'CARD_SPECIAL_TARIFF_PLAN',
    'CARD_SERVICE_PACKAGE',
    'CARD_INDIVIDUAL_DESIGN',
    'CARD_TRANSPORT_APP',
    'TYPE_OF_OFFER'
    );

delete
from dimension;

WITH RECURSIVE t(n) AS (
    VALUES (1)
    UNION ALL
    SELECT n + 1
    FROM t
    WHERE n < 2
)
SELECT 3
FROM t;

create or replace function search_dimension_parents(dimension_id bigint) returns table
                                                                                 (
                                                                                     parent_id bigint
                                                                                 )
as
$$
begin
    return query
        with recursive search_graph(start_id) as
                           (
                               select broader
                               from dimension
                               where id = dimension_id
                               union
                               select broader
                               from dimension
                                        join search_graph on dimension.id = search_graph.start_id
                               where broader is not null
                           )
        select *
        from search_graph;
end;
$$ language plpgsql;

create or replace function is_branch(arr bigint[])
    returns boolean as
$$
declare
    res bigint[];
begin
    res := arr[1:idx - 1];
    res[idx] = elem;
    res := res || arr[:idx];
    return res;
end
$$ language plpgsql;

select *
from dimension
where id = any ('{1,2,3}');

select search_dimension_parents(12);

SELECT version();

delete
from observation;


create table observation_dimension_v2
(
    dimension_id      bigint      not null
        constraint observation_dimension_dimension_id_fk
            references dimension
            on update cascade on delete cascade,
    obs_dimension_id  bigint      not null
        constraint observation_dimension_obs_dimension_id_fk
            references dimension
            on update cascade on delete cascade,
    observation_id    bigint      not null
        constraint observation_dimension_observation_id_fk
            references observation
            on update cascade on delete cascade,
    dimension_subtype varchar(25) not null
);

alter table observation_dimension_v2
    owner to postgres;

select observation_id, dimension_id, unnest(all_narrower) obs_dimension_id
from observation_dimension od
         join dimension d on d.id = od.dimension_id
where dimension_id = 471;

select *
from (select *, unnest(all_narrower) as n from dimension) d
where n = 471;

insert into observation_dimension_v2 (observation_id, dimension_id, obs_dimension_id)
select observation_id, dimension_id, obs_dimension_id
from (
         select observation_id as observation_id, dimension_id as dimension_id, unnest(all_narrower) as obs_dimension_id
         from observation_dimension od
                  join dimension d on d.id = od.dimension_id
         union
         select observation_id as observation_id, dimension_id as dimension_id, dimension_id as obs_dimension_id
         from observation_dimension
     ) as ins;

explain analyse verbose
    select o.id, o.str_id
    from observation o
             join observation_dimension_v2 od on o.id = od.observation_id
             join dimension d on od.dimension_id = d.id
    where o.id in (501, 502, 503)
      and od.dimension_id = od.obs_dimension_id;

create index observation_dimension_v2_obs_dim_idx on observation_dimension_v2 ((dimension_id = obs_dimension_id));
drop index observation_dimension_v2_obs_dim_idx;

select array_dims(array_fill(7, ARRAY [3], ARRAY [3]));
SELECT array_fill('hi'::text, array [3]);

update observation_dimension_v2 od
set dimension_subtype = (select subtype from dimension where id = od.obs_dimension_id);

select distinct dimension_subtype, observation_id, dimension_id, str_id
from observation_dimension_v2 od
         join dimension d on d.id = od.dimension_id
where observation_id = 949;

select distinct dimension_subtype, observation_id, dimension_id, str_id
from observation_dimension_v2 od
         join dimension d on d.id = od.dimension_id
where observation_id in (1696, 949, 2876, 2878);


select dimension_subtype
from (
         select dimension_subtype, count(1)
         from observation_dimension_v2
         where observation_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
         group by dimension_subtype, dimension_id
         having count(1) != 10
     ) od
         join dimension_subtype ds on od.dimension_subtype = ds.subtype
order by ds.num desc;

select dimension_subtype, dimension_id
from observation_dimension_v2
where observation_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
group by dimension_subtype, dimension_id;

select dimension_subtype, dimensions
from (
         select dimension_subtype, array_agg(dimension_id) dimensions
         from (
                  select distinct dimension_subtype, dimension_id
                  from observation_dimension_v2
                  where observation_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
              ) od_distinct
         group by dimension_subtype
         having cardinality(array_agg(dimension_id)) > 1
     ) od
         join dimension_subtype ds on ds.subtype = od.dimension_subtype
order by num asc;

select *
from observation_dimension_v2
where observation_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
  and dimension_subtype = 'CLIENT_CATEGORY';

select dimension_subtype
from (
         select distinct dimension_subtype, array_agg(dimension_id)
         from (
                  select dimension_subtype, dimension_id
                  from observation_dimension_v2
                  where observation_id in (1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
              ) od_distinct
         group by dimension_subtype, dimension_id
         having count(1) != 10
     ) od
         join dimension_subtype ds on od.dimension_subtype = ds.subtype
order by ds.num asc;


select observation_id
from (
         select observation_id, array_agg(obs_dimension_id) obs_array
         from observation_dimension_v2
         group by observation_id
     ) obs
where obs_array <@ (
    select array_agg(id)
    from (
             select id
             from dimension
             where id in
                   (104, 87, 9, 3, 443, 72, 448, 37, 42, 234, 461, 114, 117, 93, 69, 466, 463, 432, 453, 107, 65, 422,
                    4)
             union
             select unnest(all_narrower) child_id
             from dimension
             where id in
                   (104, 87, 9, 3, 443, 72, 448, 37, 42, 234, 461, 114, 117, 93, 69, 466, 463, 432, 453, 107, 65, 422,
                    4)
         ) as aggregated);

select observation_id,
       dimension_subtype,
       array_agg(obs_dimension_id order by obs_dimension_id),
       array_agg(distinct dimension_id)
from observation_dimension_v2
where observation_id = 949
group by observation_id, dimension_subtype;


select observation_id, array_agg(dimension_subtype)
from (
         select observation_id,
                dimension_subtype,
                array_agg(obs_dimension_id order by obs_dimension_id) dim_ids
         from observation_dimension_v2
         where observation_id = 949
         group by observation_id, dimension_subtype
     ) as obs
         join (
    select subtype, array_agg(id order by id) dim_ids
    from (
             select id, subtype
             from dimension
             where id in (:ids)
             union
             select unnest(all_narrower) child_id, subtype
             from dimension
             where id in (:ids)
         ) unioned
    group by subtype
) as ids
              on obs.dimension_subtype = ids.subtype and obs.dim_ids && ids.dim_ids
group by observation_id
having cardinality(array_agg(dimension_subtype)) = (select count(1) from dimension_subtype);

select observation_id, cardinality(array_agg(obs.dimension_subtype))
from (
         select observation_id,
                dimension_subtype,
                array_agg(obs_dimension_id order by obs_dimension_id) dim_ids
         from observation_dimension_v2
         where observation_id = 949
         group by observation_id, dimension_subtype
     ) as obs
         join (
    select subtype, array_agg(id order by id) dim_ids
    from (
             select id, subtype
             from dimension
             where id in
                   (104, 87, 9, 3, 443, 72, 448, 37, 42, 234, 461, 114, 117, 93, 69, 466, 463, 432, 453, 107, 65, 422,
                    4)
             union
             select unnest(all_narrower) child_id, subtype
             from dimension
             where id in
                   (104, 87, 9, 3, 443, 72, 448, 37, 42, 234, 461, 114, 117, 93, 69, 466, 463, 432, 453, 107, 65, 422,
                    4)
         ) unioned
    group by subtype
) as ids
              on obs.dimension_subtype = ids.subtype and obs.dim_ids && ids.dim_ids
group by observation_id;