package plain.pojos;

public class CryptoCurrency {

   public final String description;

   public final Integer rank;

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

}
