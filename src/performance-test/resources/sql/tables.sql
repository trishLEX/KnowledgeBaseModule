create table performance.dimension (like public.dimension including all);
insert into performance.dimension select * from test.dimension;

create table performance.dimension_subtype (like public.dimension_subtype including all);
insert into performance.dimension_subtype select * from test.dimension_subtype;

create table performance.observation (like public.observation including all);
insert into performance.observation select * from test.observation;

create table performance.observation_dimension_v2 (like public.observation_dimension_v2 including all);
insert into performance.observation_dimension_v2 select * from test.observation_dimension_v2;

create table performance.observation_value (like public.observation_value including all);
insert into performance.observation_value select * from test.observation_value;

create table performance.value (like public.value including all);
insert into performance.value select * from test.value;