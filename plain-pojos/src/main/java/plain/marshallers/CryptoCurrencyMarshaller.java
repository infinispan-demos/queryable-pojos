package plain.marshallers;

import org.infinispan.protostream.MessageMarshaller;
import plain.pojos.CryptoCurrency;

import java.io.IOException;

public class CryptoCurrencyMarshaller implements MessageMarshaller<CryptoCurrency> {

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
   public Class<CryptoCurrency> getJavaClass() {
      return CryptoCurrency.class;
   }

   @Override
   public String getTypeName() {
      return "crypto.CryptoCurrency";
   }

}
