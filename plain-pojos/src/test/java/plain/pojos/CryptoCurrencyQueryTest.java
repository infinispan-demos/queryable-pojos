package plain.pojos;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;
import static org.junit.Assert.assertEquals;

public class CryptoCurrencyQueryTest {

   @Test
   public void testCryptoCurrencyQuery() {
      ConfigurationBuilder cfg = new ConfigurationBuilder();
      cfg.addServer()
         .host("localhost")
         .port(11222)
         .marshaller(new ProtoStreamMarshaller())
         .clientIntelligence(ClientIntelligence.BASIC);

      RemoteCacheManager remote = new RemoteCacheManager(cfg.build());

      // Create an indexed cache out of templates
      RemoteCache<String, CryptoCurrency> cache =
         remote.administration().getOrCreateCache("cryptos", "indexed");

      // Add proto file for indexed pojo to server and configure client marshallers
      addProtofileMetadata(remote);

      // Clear cache
      cache.clear();

      cache.put("BTC", new CryptoCurrency("Bitcoin", 1));
      cache.put("ETH", new CryptoCurrency("Ethereum", 2));
      cache.put("XRP", new CryptoCurrency("Ripple", 3));
      cache.put("CAT", new CryptoCurrency("Catcoin", 618));

      assertEquals("Catcoin", cache.get("CAT").description);
      assertEquals(4, cache.size());

      QueryFactory queryFactory = Search.getQueryFactory(cache);

      Query query = queryFactory.create("FROM plain.pojos.CryptoCurrency c where c.rank < 10");
      List<CryptoCurrency> highRankCoins = query.list();

      assertEquals(3, highRankCoins.size());
      System.out.printf("Highest ranked crypto currencies are: %s%n", highRankCoins);
   }

   private void addProtofileMetadata(RemoteCacheManager remote) {
      SerializationContext serialCtx =
         ProtoStreamMarshaller.getSerializationContext(remote);

      RemoteCache<String, String> metadataCache =
         remote.getCache(PROTOBUF_METADATA_CACHE_NAME);

      String protoFile = read(CryptoCurrencyQueryTest.class.getResourceAsStream("/crypto.proto"));

      metadataCache.put("crypto.proto", protoFile);

      String filesWithErrors = metadataCache.get(".errors");
      if (filesWithErrors != null)
         throw new AssertionError("Errors found in proto file(s): " + filesWithErrors);

      // Client side
      try {
         serialCtx.registerProtoFiles(FileDescriptorSource.fromResources("/crypto.proto"));
         serialCtx.registerMarshaller(new CryptoCurrency.Marshaller());
      } catch (IOException e) {
         throw new AssertionError(e);
      }
   }

   private static String read(InputStream is) {
      try (Scanner scanner = new Scanner(is, "UTF-8")) {
         return scanner.useDelimiter("\\A").next();
      }
   }
}
