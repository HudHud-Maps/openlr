## Overview
In this document, we're going to focus on how to encode and decode OpenLR locations 
on digital maps using modules in this package.

## SetUp
We'll need to add the following dependencies to our pom.xml
<p>Note: Replace ${openlr.version} with the version of the library you wish to use.</p>

```xml
<dependency>
 <groupId>org.openlr</groupId>
 <artifactId>encoder</artifactId>
 <version>${openlr.version}</version>
</dependency>
```
```xml
<dependency>
 <groupId>org.openlr</groupId>
 <artifactId>decoder</artifactId>
 <version>${openlr.version}</version>
</dependency>
```
```xml
<dependency>
 <groupId>org.openlr</groupId>
 <artifactId>map</artifactId>
 <version>${openlr.version}</version>
</dependency>
```
```xml
<dependency>
 <groupId>org.openlr</groupId>
 <artifactId>data</artifactId>
 <version>${openlr.version}</version>
</dependency>
```
```xml
<dependency>
 <groupId>org.openlr</groupId>
 <artifactId>binary</artifactId>
 <version>${openlr.version}</version>
</dependency>
```
```xml
<dependency>
 <groupId>org.openlr</groupId>
 <artifactId>xml</artifactId>
 <version>${openlr.version}</version>
</dependency>
```

```xml
<dependency>
 <groupId>org.openlr</groupId>
 <artifactId>proto</artifactId>
 <version>${openlr.version}</version>
</dependency>
```

## Map Implementation

**Implement the following interfaces against the map you want to use**

<ul>
<li>openlr.map.Line</li>
<li>openlr.map.Node</li>
<li>openlr.map.MapDatabase</li>
</ul> 

## Load Map

<p> We are using the sqlite database for the remaining part of the document </p>

```java
MapLoadParameter param = new DBFileNameParameter();
param.setValue("path/to/map");
OpenLRMapLoader loader = new SQLiteMapLoader();
MapDatabase map = loader.load(Arrays.asList(param));
```

## Physical Format

<p>Choose the physical format of the OpenLR location reference container<br>
The following three formats are available in this package:</p>
<ul>
<li>binary: openlr.binary</li>
<li>protobuf: openlr.proto</li>
<li>xml: openlr.xml</li>
</ul>

<p>We are choosing binary format for the remaining part of the document</p>

## Encoder

<p>The code snippet given below shows how to encode a line location on the map in OpenLR binary format.</p>

<p>you can find the default encoder properties file in openlr/encoder/src/main/resources/OpenLR-Encoder-Properties.xml
</p>

***In the example given below we are encoding a line location with connected lines (1,2,3,4)***

```java
//Build the line location
List<Line>  testLocation = Arrays.asList(mapDatabaseAdapter.getLine(1),
                                         mapDatabaseAdapter.getLine(2),
                                         mapDatabaseAdapter.getLine(3),
                                         mapDatabaseAdapter.getLine(4));
Location location = LocationFactory.createLineLocation("Test location", testLocation, 0, 0);

//Initialize the physical format encoder
PhysicalEncoder physicalEncoder = new OpenLRBinaryEncoder();

//Build the encoder configurations
Configuration encoderConfig = OpenLRPropertiesReader.loadPropertiesFromFile(new File("OpenLR-Encoder-Properties.xml"));
OpenLREncoderParameter params = new OpenLREncoderParameter.Builder().with(map).with(encoderConfig)
                .with(Arrays.asList(physicalEncoder))
                .buildParameter();

//Initialize the onpenlr encoder
OpenLREncoder encoder = new openlr.encoder.OpenLREncoder();

//Encode the line location in openlr format
LocationReferenceHolder locationReferenceHolder = encoder.encodeLocation(params, location);

//Generate the binary for the openlr location referencing container
String locationReferenceBinary = ((ByteArray) physicalEncoder.encodeData(locationReferenceHolder.getRawLocationReferenceData()).getLocationReferenceData()).getBase64Data();
```

## Decoder

<p>The code snippet given below shows how to decode an OpenLR binary location reference on to a map.</p>
<p>you can find the default encoder properties file in openlr/decoder/src/main/resources/OpenLR-Decoder-Properties.xml</p>

***In the example given below we are decoding OpenLR location reference binary string***

```java
//base64 openlr binary string  to decode 
String openlr = "CwmQ9SVWJS2qBAD9/14tCQ==";

//Initialize the binary decoder and decode the binary string
OpenLRBinaryDecoder binaryDecoder = new OpenLRBinaryDecoder();
ByteArray byteArray = new ByteArray(Base64.getDecoder().decode(openlr));
LocationReferenceBinaryImpl locationReferenceBinary = new LocationReferenceBinaryImpl("Test location", byteArray);
RawLocationReference rawLocationReference = binaryDecoder.decodeData(locationReferenceBinary);

//Build the decoder configuration
Configuration decoderConfig = OpenLRPropertiesReader.loadPropertiesFromFile(new File(TestMapStubTest.class.getClassLoader().getResource("OpenLR-Decoder-Properties.xml").getFile()));
OpenLRDecoderParameter params = new OpenLRDecoderParameter.Builder().with(map).with(decoderConfig).buildParameter();

//Initialize the decoder
OpenLRDecoder decoder = new openlr.decoder.OpenLRDecoder();

//decode the location
Location location = decoder.decodeRaw(params, rawLocationReference);
```
