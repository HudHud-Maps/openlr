DROP FUNCTION IF EXISTS print_line_strings(ways bigint[]);
CREATE OR REPLACE FUNCTION print_line_strings(ways bigint[])
RETURNS text[] AS $$
DECLARE
    route geometry;
    f_line text;
BEGIN
    FOR i IN 1..array_length(ways, 1) LOOP
        SELECT construct_route(ARRAY[ways[i]]) INTO route;
        f_line := ST_AsText(route);
    END LOOP;
    SELECT construct_route(ways) INTO route;
    f_line := ST_AsText(route);
    RETURN ARRAY[f_line];
END;