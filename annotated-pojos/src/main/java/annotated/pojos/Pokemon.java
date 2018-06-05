package annotated.pojos;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoDoc("@Indexed")
public class Pokemon {

   @ProtoField(number = 10)
   String name;

   @ProtoField(number = 20)
   String type1;

   public Pokemon() {
   }

   public Pokemon(String name, String type1) {
      this.name = name;
      this.type1 = type1;
   }

   @Override
   public String toString() {
      return "Pokemon{" +
         "name='" + name + '\'' +
         ", type1='" + type1 + '\'' +
         '}';
   }

}
