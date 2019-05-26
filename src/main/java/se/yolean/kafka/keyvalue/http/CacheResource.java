package se.yolean.kafka.keyvalue.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import se.yolean.kafka.keyvalue.KafkaCache;

@Path("/cache/v1")
public class CacheResource {

  @Inject
  KafkaCache cache;

  /**
   * Will eventually contain logic for reading values from other replicas in
   * partitioned caches
   * (or at least so we thought back in the non-sidecar model).
   *
   * @param key To look up
   * @return the value
   * @throws NotFoundException If the key wasn't in the cache or if the value
   *                           somehow was null
   */
  byte[] getCacheValue(String key) throws NotFoundException {
    if (key == null) {
      throw new javax.ws.rs.BadRequestException("Request key can not be null");
    }
    if (key == "") {
      throw new javax.ws.rs.BadRequestException("Request key can not be empty");
    }
    final byte[] value = cache.getValue(key);
    if (value == null) {
      throw new NotFoundException();
    }
    return value;
  }

  @GET
  @Path("/raw/{key}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public byte[] valueByKey(@PathParam("key") final String key, @Context UriInfo uriInfo) {
    return getCacheValue(key);
  }

  @GET
  @Path("/offset/{topic}/{partition}")
  @Produces(MediaType.TEXT_PLAIN)
  public Long getCurrentOffset(@PathParam("topic") String topic, @PathParam("partition") Integer partition) {
    if (topic == null) {
      throw new BadRequestException("Topic can not be null");
    }
    if (topic.length() == 0) {
      throw new BadRequestException("Topic can not be a zero length string");
    }
    if (partition == null) {
      throw new BadRequestException("Partition can not be null");
    }
    return cache.getCurrentOffset(topic, partition);
  }

  /**
   * All keys in this instance (none from the partitions not represented here),
   * newline separated.
   */
  @GET()
  @Path("/keys")
  public Response keys() {
    Iterator<String> all = cache.getKeys();

    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream out) throws IOException, WebApplicationException {
        while (all.hasNext()) {
          out.write(all.next().getBytes());
          out.write('\n');
        }
      }
    };
    return Response.ok(stream).build();
  }

  /**
   * All keys in this instance (none from the partitions not represented here).
   */
  @GET()
  @Path("/keys")
  @Produces(MediaType.APPLICATION_JSON)
  public Response keysJson() {
    Iterator<String> all = cache.getKeys();

    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream out) throws IOException, WebApplicationException {
        JsonGenerator json = Json.createGenerator(out);
        JsonGenerator list = json.writeStartArray();
        while (all.hasNext()) {
          list.write(all.next());
        }
        list.writeEnd();
        json.close();
      }
    };
    return Response.ok(stream).build();
  }

  /**
   * @return Newline separated values (no keys)
   */
  @GET()
  @Path("/values")
  @Produces(MediaType.TEXT_PLAIN)
  public Response values() {
    Iterator<byte[]> values = cache.getValues();

    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream out) throws IOException, WebApplicationException {
        while (values.hasNext()) {
          out.write(values.next());
          out.write('\n');
        }
      }
    };
    return Response.ok(stream).build();
  }

}
