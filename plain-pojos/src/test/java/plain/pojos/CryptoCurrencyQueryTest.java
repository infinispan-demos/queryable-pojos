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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.List;

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

      assertEquals(cache.get("CAT").description, "Catcoin");
      assertEquals(cache.size(), 4);

      QueryFactory queryFactory = Search.getQueryFactory(cache);

      Query query = queryFactory.create("FROM plain.pojos.CryptoCurrency c where c.rank < 10");
      List<CryptoCurrency> highRankCoins = query.list();

      assertEquals(highRankCoins.size(), 3);
      System.out.printf("Highest ranked crypto currencies are: %s%n", highRankCoins);
   }

   private void addProtofileMetadata(RemoteCacheManager remote) {
      SerializationContext serialCtx =
         ProtoStreamMarshaller.getSerializationContext(remote);

      RemoteCache<String, String> metadataCache =
         remote.getCache(PROTOBUF_METADATA_CACHE_NAME);

      try {
         String protoFile = read(CryptoCurrencyQueryTest.class.getResourceAsStream("/crypto.proto"));

         metadataCache.put("crypto.proto", protoFile);

         String errors = metadataCache.get(".errors");
         if (errors != null)
            throw new AssertionError("Errors found in proto file: " + errors);
      } catch (IOException e) {
         throw new AssertionError(e);
      }

      // Client side
      try {
         serialCtx.registerProtoFiles(FileDescriptorSource.fromResources("crypto.proto"));
         serialCtx.registerMarshaller(new CryptoCurrency.Marshaller());
      } catch (IOException e) {
         throw new AssertionError(e);
      }
   }

   private static String read(InputStream is) throws IOException {
      try {
         final Reader reader = new InputStreamReader(is, "UTF-8");
         StringWriter writer = new StringWriter();
         char[] buf = new char[1024];
         int len;
         while ((len = reader.read(buf)) != -1) {
            writer.write(buf, 0, len);
         }
         return writer.toString();
      } finally {
         is.close();
      }
   }

}
