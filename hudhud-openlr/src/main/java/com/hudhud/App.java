package com.hudhud;
import java.io.File;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;

import com.hudhud.impl.MapDatabaseImpl;

import openlr.binary.ByteArray;
import openlr.binary.OpenLRBinaryDecoder;
import openlr.binary.impl.LocationReferenceBinaryImpl;
import openlr.decoder.OpenLRDecoder;
import openlr.decoder.OpenLRDecoderParameter;
import openlr.location.Location;
import openlr.map.FormOfWay;
import openlr.map.FunctionalRoadClass;
import openlr.map.Line;
import openlr.map.Node;
import openlr.properties.OpenLRPropertiesReader;
import openlr.rawLocRef.RawLocationReference;
import openlr.map.GeoCoordinates;
import openlr.map.GeoCoordinatesImpl;
/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(final String[] args )
    {
        String url = "jdbc:postgresql://postgis/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "postgres");
        props.setProperty("ssl", "false");
        try {
            String openlr = "\013!<\226\021\220w\032d$\006\253\004D\032\024";
            byte[] byteArray = new byte[openlr.length()];

            for (int i = 0; i < openlr.length(); i++) {
                byteArray[i] = (byte) openlr.charAt(i);
            }
            Connection conn = DriverManager.getConnection(url, props);
            MapDatabaseImpl mapDatabase = new MapDatabaseImpl(conn);
            OpenLRBinaryDecoder binaryDecoder = new OpenLRBinaryDecoder();
            ByteArray byteArrays = new ByteArray(byteArray);
            LocationReferenceBinaryImpl locationReferenceBinary = new LocationReferenceBinaryImpl("Test location", byteArrays);
            RawLocationReference rawLocationReference = binaryDecoder.decodeData(locationReferenceBinary);
            Configuration decoderConfig = OpenLRPropertiesReader.loadPropertiesFromFile(new File("OpenLR-Decoder-Properties.xml"));
            OpenLRDecoderParameter params = new OpenLRDecoderParameter.Builder().with(mapDatabase).with(decoderConfig).buildParameter();
            OpenLRDecoder decoder = new openlr.decoder.OpenLRDecoder();
            Location location = decoder.decodeRaw(params, rawLocationReference);
            System.out.println(location);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

