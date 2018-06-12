Demo comparing different ways in which remote querying can be enabled for Java POJOs.

To run the demo, start by an Infinispan Server instance locally:

```bash
docker run -it -p 11222:11222 --rm jboss/infinispan-server:9.3.0.CR1
```

To verify the annotated object method, run `annotated.pojos.PokemonQueryTest` test class.

To verify the plain object method, run `plain.pojos.CryptoCurrencyQueryTest` test class.
