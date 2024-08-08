-- DROP FUNCTION IF EXISTS show_way_speeds(ways bigint[][], speeds double precision[]);
-- CREATE OR REPLACE FUNCTION show_way_speeds(ways bigint[][], speeds double precision[])
-- RETURNS text[] AS $$
-- DECLARE
--     route geometry;
--     f_line text;
--     l_line text;
-- BEGIN
--     SELECT construct_route(ways) INTO route;
--     f_line := ST_AsText(route);
--     SELECT ST_AsText(ST_MakeLine(points_along_line(route, speeds))) INTO l_line;
--     RETURN ARRAY[f_line, l_line];
-- END;
-- $$
-- LANGUAGE plpgsql;

-- SELECT UNNEST(show_way_speeds(
-- ARRAY[ARRAY[969469576, 0, 0], ARRAY[1123396361, 0, 0], ARRAY[121936841, 6183, 0]],
-- ARRAY[0, 2081, 2303, 2534, 2838, 3062, 3522, 3816]
-- ));

-- psql -U postgres -f scripts.sql | sed -n '3,4p' | awk '{if(NR==1) print "var s = `"$0"`"; if(NR==2) print "var points_s = `"$0"`"}' | xclip -selection clipboard

-- SELECT ST_AsText(
--     construct_route(ARRAY[ARRAY[969469576, 0, 0, 0], ARRAY[1123396361, 0, 0, 0], ARRAY[121936841, 6183, 0, 0]])
-- );

-- SELECT ST_AsText(
--     construct_route(ARRAY[ARRAY[121936841, 4255, 1897, 0], ARRAY[1123396363, 0, 0, 0], ARRAY[353583775, 0, 0, 0]])
-- );

-- SELECT ST_AsText(
--     construct_route(ARRAY[ARRAY[1092022388, 0, 0, 1], ARRAY[1092022389, 0, 0, 1]])
-- );
    -- osmIds {
    --   unkownField: 1
    --   ids {
    --     osmId: 1098781541
    --     unkownField1: 1
    --   }
    --   ids {
    --     osmId: 133679690
    --     unkownField1: 1
    --     unkownField3: 270
    --   }
    --   ids {
    --     osmId: 1098781539
    --     unkownField1: 0
    --   }
    --   ids {
    --     osmId: 1098781548
    --     unkownField1: 0
    --     unkownField2: 190
    --   }
    -- }
SELECT ST_AsText(
    construct_route(ARRAY[ARRAY[1098781541, 0, 0, 1], ARRAY[133679690, 0, 0, 1], ARRAY[1098781539, 0, 0, 0], ARRAY[1098781548, 190, 0, 0]])
);