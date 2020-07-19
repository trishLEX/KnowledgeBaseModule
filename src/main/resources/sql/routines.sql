create function search_dimension_parents(dimension_id bigint) returns TABLE(parent_id bigint)
    language plpgsql
as $$
begin
    return query
        with recursive search_graph(start_id) as
                           (
                               select broader from dimension where id = dimension_id
                               union select broader from dimension join search_graph on dimension.id = search_graph.start_id
                               where broader is not null
                           )
        select * from search_graph;
end;
$$;

alter function search_dimension_parents(bigint) owner to postgres;

