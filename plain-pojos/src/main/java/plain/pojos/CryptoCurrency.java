package plain.pojos;

import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class CryptoCurrency {

   final String description;

   final Integer rank;

   public CryptoCurrency(String description, Integer rank) {
      this.description = description;
      this.rank = rank;
   }

   @Override
   public String toString() {
      return "CryptoCurrency{" +
         "description='" + description + '\'' +
         ", rank=" + rank +
         '}';
   }

   public static final class Marshaller implements MessageMarshaller<CryptoCurrency> {

      @Override
      public CryptoCurrency readFrom(ProtoStreamReader reader) throws IOException {
         String description = reader.readString("description");
         Integer rank = reader.readInt("rank");
         return new CryptoCurrency(description, rank);
      }

      @Override
      public void writeTo(ProtoStreamWriter writer, CryptoCurrency obj) throws IOException {
         writer.writeString("description", obj.description);
         writer.writeInt("rank", obj.rank);
      }

      @Override
      public Class<? extends CryptoCurrency> getJavaClass() {
         return CryptoCurrency.class;
      }

      @Override
      public String getTypeName() {
         return CryptoCurrency.class.getName();
      }

   }

}
