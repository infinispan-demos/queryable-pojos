package annotated.pojos;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.junit.Test;

import java.io.IOException;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX;
import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

public class PokemonQueryTest {

   @Test
   public void testPokemonQuery() {
      ConfigurationBuilder cfg = new ConfigurationBuilder();
      cfg.addServer()
            .host("localhost")
            .port(11222)
         .marshaller(new ProtoStreamMarshaller())
         .clientIntelligence(ClientIntelligence.BASIC);

      RemoteCacheManager remote = new RemoteCacheManager(cfg.build());

      // Create an indexed cache out of templates
      RemoteCache<Integer, Pokemon> cache =
         remote.administration().getOrCreateCache("pokemons", "indexed");

      // Add proto file for indexed pojo to server and client
      addProtofileMetadata(remote);

      cache.put(4, new Pokemon("Charmander", "FIRE"));
      cache.put(7, new Pokemon("Squirtle", "WATER"));
      cache.put(25, new Pokemon("Pikachu", "ELECTRIC"));
      cache.put(59, new Pokemon("Arcanine", "FIRE"));
      cache.put(116, new Pokemon("Horsea", "WATER"));
      cache.put(136, new Pokemon("Flareon", "FIRE"));

      System.out.printf("Pokemon with id %d is: %s%n", 25, cache.get(25));
      System.out.printf("Stored %d pokemons%n", cache.size());
   }

   private void addProtofileMetadata(RemoteCacheManager remote) {
      SerializationContext serialCtx =
         ProtoStreamMarshaller.getSerializationContext(remote);

      RemoteCache<String, String> metadataCache =
         remote.getCache(PROTOBUF_METADATA_CACHE_NAME);

      ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();

      try {
         final String fileName = "pokemon.proto";
         String protoFile = protoSchemaBuilder
            .fileName(fileName)
            .addClass(Pokemon.class)
            .build(serialCtx);

         metadataCache.put(fileName, protoFile);


         String errors = metadataCache.get(ERRORS_KEY_SUFFIX);
         if (errors != null)
            throw new AssertionError("Error in proto file");
         else
            System.out.println("Added indexed file " + fileName);
      } catch (IOException e) {
         throw new AssertionError(e);
      }
   }

}
