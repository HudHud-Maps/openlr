package com.hudhud.impl;

import java.awt.geom.Rectangle2D.Double;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import openlr.map.FormOfWay;
import openlr.map.FunctionalRoadClass;
import openlr.map.GeoCoordinates;
import openlr.map.GeoCoordinatesImpl;
import openlr.map.InvalidMapDataException;
import openlr.map.Line;
import openlr.map.Node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;
public class MapDatabaseImpl implements openlr.map.MapDatabase {
    private static final Logger LOG = LogManager.getLogger(MapDatabaseImpl.class);
    private Connection connection;
    public MapDatabaseImpl(Connection connection) {
        // TODO Auto-generated constructor stub
        this.connection = connection;
    }
    @Override
    public boolean hasTurnRestrictions() {
        return false;
    }

    @Override
    public Line getLine(long id) {
        try {
            // PreparedStatement st = connection.prepareStatement("SELECT nodes[1] AS start, (nodes)[array_length(nodes, 1)] AS end FROM planet_osm_ways WHERE id = ?");
            // st.setLong(1, id);
            // ResultSet rs = st.executeQuery();
            // // if (rs.next()) {
            // //     return new NodeImpl(rs.getLong("id"), rs.getDouble("longitude"), rs.getDouble("latitude"));
            // // }
            // rs.next();
            // long start = rs.getLong("start");
            // long end = rs.getLong("end");
            // PreparedStatement st2 = connection.prepareStatement("SELECT * FROM planet_osm_roads WHERE osm_id = ?");
            // st2.setLong(1, id);
            // ResultSet rs2 = st2.executeQuery();
            // rs2.next();
            // System.out.println(id  + "IDDDDD");
            // String highway = rs2.getString("highway");
            // String junction = rs2.getString("junction");
            // FormOfWay fow = getFormOfWay(highway, junction);
            // FunctionalRoadClass frc = getFunctionalRoadClass(highway);
            // PreparedStatement st3 = connection.prepareStatement("SELECT pon.id, pon.lat::float / 10000000 AS lat, pon.lon::float / 10000000 AS lon FROM planet_osm_ways pow LEFT JOIN planet_osm_nodes pon ON pon.id = ANY(pow.nodes) WHERE pow.id = ? ORDER BY array_position(pow.nodes, pon.id)");
            // st3.setLong(1, id);
            // ResultSet rs3 = st3.executeQuery();
            // List<GeoCoordinates> geos = new java.util.ArrayList<GeoCoordinates>();
            // while (rs3.next()) {
            //     geos.add(new GeoCoordinatesImpl(rs3.getDouble("lon"), rs3.getDouble("lat")));
            // }
            // PreparedStatement st4 = connection.prepareStatement("SELECT ST_Length(way) AS length FROM planet_osm_roads WHERE osm_id = ?");
            // st4.setLong(1, id);
            // ResultSet rs4 = st4.executeQuery();
            // rs4.next();
            // double length = rs4.getDouble("length");
            // Map<Locale, List<String>> names = new HashMap<Locale, List<String>>();
            // // convert double to int
            // int lengthM = (int) Math.round(length);
            // // create list of names that contains one name
            // names.put(Locale.forLanguageTag("ar"), Arrays.asList(rs2.getString("name")));
            // rs.close();
            // rs2.close();
            // rs3.close();
            // rs4.close();

            // PreparedStatement ps = connection.prepareStatement("""
            //     SELECT nodes[1] AS start, (nodes)[array_length(nodes, 1)] AS end FROM hudhud_ways WHERE way_id = ?
            // """);
            String query = "SELECT"
                + " nodes[1] AS start,"
                + " (nodes)[array_length(nodes, 1)] AS end,"
                + " highway,"
                + " junction,"
                + " ST_AsText(geom) AS t_nodes,"
                + " ST_Length(geom_3857)::int AS length,"
                + " name"
                + " FROM hudhud_ways"
                + " WHERE way_id = ?";
            PreparedStatement st = connection.prepareStatement(query);
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            rs.next();
            long start = rs.getLong("start");
            long end = rs.getLong("end");
            String highway = rs.getString("highway");
            String junction = rs.getString("junction");
            FormOfWay fow = getFormOfWay(highway, junction);
            FunctionalRoadClass frc = getFunctionalRoadClass(highway);
            String t_nodes = rs.getString("t_nodes");
            String[] nodes = t_nodes.substring(11, t_nodes.length() - 1).split(",");
            List<GeoCoordinates> geos = new java.util.ArrayList<GeoCoordinates>();
            for (String node : nodes) {
                String[] latlng = node.split(" ");
                double lat = java.lang.Double.parseDouble(latlng[1]);
                double lng = java.lang.Double.parseDouble(latlng[0]);
                GeoCoordinates geo = new GeoCoordinatesImpl(lat, lng);
                geos.add(geo);
            }
            int length = rs.getInt("length");
            String name = rs.getString("name");
            Map<Locale, List<String>> names = new HashMap<Locale, List<String>>();
            names.put(Locale.forLanguageTag("ar"), Arrays.asList(name));
            return new LineImpl(connection, this, id, start, end, fow, frc, geos, length, names);
        } catch (Exception e) {
            LOG.error(e);
            e.printStackTrace();
            return null;
        } 
    }
    private FormOfWay getFormOfWay(String fow, String junction) {
        if (junction != null && junction == "roundabout") {
            return FormOfWay.ROUNDABOUT;
        }
        if (fow == null) {
            return FormOfWay.UNDEFINED;
        }
        switch (fow) {
            case "secondary_link":
                return FormOfWay.SLIPROAD;
            case "trunk":
                return FormOfWay.SINGLE_CARRIAGEWAY;
            case "primary":
                return FormOfWay.SINGLE_CARRIAGEWAY;
            case "primary_link":
                return FormOfWay.SLIPROAD;
            case "motorway_link":
                return FormOfWay.SLIPROAD;
            case "secondary":
                return FormOfWay.SINGLE_CARRIAGEWAY;
            case "service":
                return FormOfWay.TRAFFIC_SQUARE;
            case "motorway":
                return FormOfWay.MOTORWAY;
            case "trunk_link":
                return FormOfWay.SLIPROAD;
            case "tertiary":
                return FormOfWay.SINGLE_CARRIAGEWAY;
            case "residential":
                return FormOfWay.SINGLE_CARRIAGEWAY;
            default:
                return FormOfWay.UNDEFINED;
        }
    }
    
    private FunctionalRoadClass getFunctionalRoadClass(String highway) {
        if (highway == null) {
            return FunctionalRoadClass.FRC_7;
        }
        switch (highway) {
            case "tertiary":
                return FunctionalRoadClass.FRC_5;
            case "secondary_link":
                return FunctionalRoadClass.FRC_4;
            case "trunk":
                return FunctionalRoadClass.FRC_1;
            case "primary":
                return FunctionalRoadClass.FRC_2;
            case "primary_link":
                return FunctionalRoadClass.FRC_4;
            case "motorway_link":
                return FunctionalRoadClass.FRC_4;
            case "secondary":
                return FunctionalRoadClass.FRC_3;
            case "service":
                return FunctionalRoadClass.FRC_6;
            case "motorway":
                return FunctionalRoadClass.FRC_0;
            case "trunk_link":
                return FunctionalRoadClass.FRC_4;
            case "residential":
                return FunctionalRoadClass.FRC_6;
            default:
                return FunctionalRoadClass.FRC_7;
        }
    }
    @Override
    public Node getNode(long id) {
        try {
            PreparedStatement st = connection.prepareStatement("SELECT ST_X(geom) AS lon, ST_Y(geom) AS lat FROM hudhud_nodes WHERE node_id = ?");
            st.setLong(1, id);
            ResultSet rs = st.executeQuery();
            // if (rs.next()) {
            //     return new NodeImpl(rs.getLong("id"), rs.getDouble("longitude"), rs.getDouble("latitude"));
            // }
            rs.next();
            double lat = rs.getDouble("lat");
            double lon = rs.getDouble("lon");
            PreparedStatement st2 = connection.prepareStatement("SELECT * FROM hudhud_ways WHERE (nodes)[array_length(nodes, 1)] = ?");
            st2.setLong(1, id);
            ResultSet rs2 = st2.executeQuery();
            Set<Long> incoming = new HashSet<Long>();
            while (rs2.next()) {
                final long lineId = rs2.getLong("way_id");
                incoming.add(lineId);
            }
            rs2.close();
            rs.close();
            PreparedStatement st3 = connection.prepareStatement("SELECT * FROM hudhud_ways WHERE (nodes)[1] = ?");
            st3.setLong(1, id);
            ResultSet rs3 = st3.executeQuery();
            Set<Long> outgoing = new HashSet<Long>();
            while (rs3.next()) {
                final long lineId = rs3.getLong("way_id");
                outgoing.add(lineId);
            }
            rs3.close();
            return new NodeImpl(this, id, lon, lat, incoming, outgoing);
        } catch (SQLException e) {
            System.out.println("ID" + id);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Iterator<Node> findNodesCloseByCoordinate(double longitude, double latitude, int distance) {
    //     SELECT id FROM planet_osm_nodes WHERE ST_DWithin(
    // ST_Transform(ST_SetSRID(ST_MakePoint(46.73941, 24.70018), 4326), 3857),
    // ST_Transform(ST_SetSRID(ST_MakePoint(lon::float / 10000000, lat::float / 10000000), 4326), 3857), 
    // 100
// );        
        final Set<Node> nodesCloseBy = new HashSet<Node>();
        ResultSet rs = null;
        try {
            PreparedStatement st = connection.prepareStatement("SELECT node_id FROM hudhud_nodes WHERE ST_DWithin(ST_Transform(ST_SetSRID(ST_MakePoint(?, ?), 4326), 3857), geom_3857, ?)");
            st.setDouble(1, longitude);
            st.setDouble(2, latitude);
            st.setInt(3, distance);
            rs = st.executeQuery();
            while (rs.next()) {
                final long id = rs.getLong("node_id");
                nodesCloseBy.add(getNode(id));
            }
            return nodesCloseBy.iterator();
        } catch (Exception e) {
            return null;
        } finally {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        }
    }

    @Override
    public Iterator<Line> findLinesCloseByCoordinate(double longitude, double latitude, int distance) {
        final Set<Line> linesCloseBy = new HashSet<Line>();
        ResultSet rs = null;
        try {
            PreparedStatement st = connection.prepareStatement("SELECT way_id AS id FROM hudhud_ways WHERE ST_DWithin(ST_Transform(ST_SetSRID(ST_MakePoint(?,?), 4326), 3857),geom_3857,?)");
            st.setDouble(1, longitude);
            st.setDouble(2, latitude);
            st.setInt(3, distance);
            rs = st.executeQuery();
            while (rs.next()) {
                final long id = rs.getLong("id");
                linesCloseBy.add(getLine(id));
            }
            return linesCloseBy.iterator();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        } finally {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        }
    }

    @Override
    public boolean hasTurnRestrictionOnPath(List<? extends Line> path) {
        return false;
    }

    @Override
    public Iterator<Node> getAllNodes() {
        final Set<Node> nodes = new HashSet<Node>(); 
        ResultSet rs = null;
        try {
            PreparedStatement st = connection.prepareStatement("SELECT node_id FROM hudhud_nodes");
            rs = st.executeQuery();
            while (rs.next()) {
                final long id = rs.getLong("id");
                nodes.add(getNode(id));
            }
            return nodes.iterator();
        } catch (Exception e) {
            LOG.error(e);
            return null;
        } finally {    
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        }
    }

    @Override
    public Iterator<Line> getAllLines() {
        final Set<Line> lines = new HashSet<Line>(); 
        ResultSet rs = null;
        try {
            PreparedStatement st = connection.prepareStatement("SELECT way_id FROM hudhud_ways");
            rs = st.executeQuery();
            while (rs.next()) {
                final long id = rs.getLong("id");
                lines.add(getLine(id));
            }
            return lines.iterator();
        } catch (SQLException e) {
            System.out.println(e);
            return null;
        } finally {    
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        }
    }

    @Override
    public Double getMapBoundingBox() {
        return null;
    }

    @Override
    public int getNumberOfNodes() {
        return -1;
    }

    @Override
    public int getNumberOfLines() {
        return -1;
    }

    
}