
DROP FUNCTION IF EXISTS truncate_way(bigint[]);
CREATE OR REPLACE FUNCTION truncate_way(params bigint[])
RETURNS geometry AS $$
DECLARE
    w_id bigint;
    distance_from_start double precision := 0;
    distance_from_end double precision := 0;
    way_geom geometry;
    way_length double precision;
    start_fraction double precision;
    end_fraction double precision;
    truncated_geom geometry;
    reverse int;
BEGIN
    -- Extract parameters from the input array
    w_id := params[1];
    distance_from_start := params[2];
    distance_from_end := params[3];
    reverse := params[4]; 
    -- Get the geometry for the given w_id (using geom_3857)
    IF distance_from_start = 0 AND distance_from_end = 0 THEN
        IF reverse = 0 THEN
            SELECT geom INTO way_geom FROM hudhud_ways WHERE way_id = w_id;
        ELSE
            SELECT ST_Reverse(geom) INTO way_geom FROM hudhud_ways WHERE way_id = w_id;
        END IF;
        RETURN way_geom;
    END IF;
    IF reverse = 0 THEN
        SELECT geom_3857 INTO way_geom FROM hudhud_ways WHERE way_id = w_id;
    ELSE
        SELECT ST_Reverse(geom_3857) INTO way_geom FROM hudhud_ways WHERE way_id = w_id;
    END IF;
    -- Check if the way exists
    -- IF way_geom IS NULL THEN
    --     RAISE EXCEPTION 'Way with ID % not found', w_id;
    -- END IF;

    -- Calculate the total length of the way
    way_length := ST_Length(way_geom);

    -- Calculate start and end fractions
    start_fraction := LEAST(distance_from_start / way_length, 1);
    end_fraction := GREATEST(1 - (distance_from_end / way_length), 0);

    -- Ensure start_fraction is less than end_fraction
    -- IF start_fraction >= end_fraction THEN
    --     RAISE EXCEPTION 'Invalid truncation: start point would be after or at the end point';
    -- END IF;

    -- Truncate the linestring
    IF end_fraction < start_fraction THEN
        RETURN ST_Transform(way_geom, 4326);
    END IF;
    truncated_geom := ST_LineSubstring(way_geom, start_fraction, end_fraction);

    -- Ensure the result is a linestring
    IF ST_GeometryType(truncated_geom) != 'ST_LineString' THEN
        RAISE EXCEPTION 'Truncation resulted in a non-linestring geometry';
    END IF;

    -- Transform the result to SRID 4326
    RETURN ST_Transform(truncated_geom, 4326);
END;
$$
 LANGUAGE plpgsql;

-- DROP FUNCTION IF EXISTS construct_route(bigint[][]);
-- CREATE OR REPLACE FUNCTION construct_route(ways bigint[][])
-- RETURNS geometry AS $$
-- DECLARE
--     g geometry(LineString,4326);
--     i integer;
--     current_way geometry;
--     intersection_point geometry;
--     fraction float;
--     remaining_way geometry;
--     g_fraction float;
--     start_way geometry;
--     way bigint[];
--     first_element boolean := true;
-- BEGIN
--     FOREACH way SLICE 1 IN ARRAY ways LOOP
--         IF first_element THEN
--             g := (SELECT truncate_way(way));
--             first_element := false;
--             CONTINUE;
--         END IF;
--         RAISE NOTICE 'Current way: %', way;
--         SELECT truncate_way(way) INTO current_way;
--         -- RAISE NOTICE 'Current way: %', ST_AsText(current_way);
--         intersection_point := ST_Intersection(g, current_way);
--         RAISE NOTICE 'Intersection point: %', ST_AsText(intersection_point);
--         g_fraction := ST_LineLocatePoint(g, intersection_point);
--         start_way := ST_LineSubstring(g, 0, g_fraction);
--         RAISE NOTICE 'Fraction: %', g_fraction;
--         IF ST_GeometryType(intersection_point) = 'ST_Point' THEN
--             fraction := ST_LineLocatePoint(current_way, intersection_point);
--             -- Get the portion of current_way from intersection to end
--             remaining_way := ST_LineSubstring(current_way, fraction, 1);
--             g := ST_LineMerge(ST_Union(start_way, remaining_way));
--         ELSE
--         -- Concatenate the current way with g
--         g := ST_LineMerge(ST_Union(g, current_way));
--         END IF;
--     END LOOP;
        
-- RETURN g;
-- END;
-- $$
-- LANGUAGE plpgsql;

DROP FUNCTION IF EXISTS construct_route(bigint[][]);
CREATE OR REPLACE FUNCTION construct_route(ways bigint[][])
RETURNS geometry AS $$
DECLARE
    g geometry(LineString,4326);
    first_element boolean := true;
    way bigint[];
    current_way geometry;
    fraction float;
    remaining_way geometry;
    intersection_point geometry(Point, 4326);
    g_fraction float;
    start_way geometry;
    closest_point geometry(Point, 4326);
    closest_point2 geometry(Point, 4326);
BEGIN
    FOREACH way SLICE 1 IN ARRAY ways LOOP
        IF first_element THEN
            g := (SELECT truncate_way(way));
            first_element := false;
            CONTINUE;
        END IF;
        -- The direction must be the same for this to work
        current_way := (SELECT truncate_way(way));
        RAISE NOTICE 'New iteration';       
        RAISE NOTICE 'G: %', ST_AsText(g);
        RAISE NOTICE 'Current way: %',  ST_AsText(current_way);
        RAISE NOTICE 'Start point: %', ST_AsText(ST_StartPoint(current_way));
        RAISE NOTICE 'End point: %', ST_AsText(ST_EndPoint(current_way));
        RAISE NOTICE 'Start point g: %', ST_AsText(ST_StartPoint(g));
        RAISE NOTICE 'End point g: %', ST_AsText(ST_EndPoint(g));
        IF ST_EndPoint(g) = ST_StartPoint(current_way) THEN
            RAISE NOTICE 'Concatenating';
            -- g := ST_LineMerge(ST_Union(g, current_way));
            g:= ST_MakeLine(g, current_way);
        ELSIF ST_Intersects(g, current_way) THEN 
            RAISE NOTICE 'Intersecting';           
            intersection_point := ST_Intersection(g, current_way);
            fraction := ST_LineLocatePoint(current_way, intersection_point);
            remaining_way := ST_LineSubstring(current_way, fraction, 1);
            g_fraction := ST_LineLocatePoint(g, intersection_point);
            start_way := ST_LineSubstring(g, 0, g_fraction);
            g := ST_MakeLine(start_way, remaining_way);
        ELSIF ST_DWithin(ST_Transform(ST_EndPoint(g), 3857), ST_Transform(ST_StartPoint(current_way), 3857), 50) THEN
            RAISE NOTICE 'Closest point';
            g := ST_MakeLine(g, current_way);
        ELSIF ST_DWithin(ST_Transform(g, 3857), ST_Transform(current_way, 3857), 10) THEN
            RAISE NOTICE 'very close lines';
            closest_point := ST_ClosestPoint(g, current_way);
            g_fraction := ST_LineLocatePoint(g, closest_point);
            RAISE NOTICE 'Start way: %', ST_AsText(ST_LineSubstring(g, 0, g_fraction));
            start_way := ST_LineSubstring(g, 0, g_fraction);
            closest_point2 := ST_ClosestPoint(current_way, closest_point);
            fraction := ST_LineLocatePoint(current_way, closest_point2);
            remaining_way := ST_LineSubstring(current_way, fraction, 1);
            g := ST_MakeLine(start_way, remaining_way);
        ELSE
            -- RAISE NOTICE 'Distance %', ST_Distance(ST_Transform(ST_EndPoint(g), 3857), ST_Transform(ST_StartPoint(current_way), 3857));
            -- -- RAISE NOTICE 'DISTANCE 2 %'
            -- -- RAISE NOTICE 'Closest point 2';
            -- RAISE NOTICE 'Distance %', ;
            -- RAISE NOTICE 'TOUCH %', ST_Touches(g, current_way);
            RAISE EXCEPTION 'No intersection or close point found';
        END IF; 
    END LOOP;
    RETURN g;
END;
$$
LANGUAGE plpgsql;















DROP FUNCTION IF EXISTS points_along_line;
CREATE OR REPLACE FUNCTION points_along_line(
    line_geom geometry,
    distances double precision[]
)
RETURNS geometry[] AS $$
DECLARE
    transformed_line geometry;
    line_length double precision;
    fraction double precision;
    point geometry;
    result geometry[] := ARRAY[]::geometry[];
    distance double precision;
BEGIN
    -- Transform the input geometry to SRID 3857
    transformed_line := ST_Transform(line_geom, 3857);

    -- Get the total length of the transformed line
    line_length := ST_Length(transformed_line);

    -- Loop through each distance in the input array
    FOREACH distance IN ARRAY distances
    LOOP
        -- Calculate the fraction of the line length
        fraction := LEAST(distance / line_length, 1.0);
        
        -- Get the point at this fraction along the line
        point := ST_Transform(ST_LineInterpolatePoint(transformed_line, fraction), ST_SRID(line_geom));
        
        -- Add the point to the result array
        result := array_append(result, point);
    END LOOP;
    
    RETURN result;
END;
$$
 LANGUAGE plpgsql;


-- problems:
-- \013\0355l\016]\\\034j\000\000 \000\001\034\025
-- offset start and end exceeds length of line