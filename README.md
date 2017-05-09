# FunctionalJava on Derive4J steroids!

This [Derive4J](https://github.com/derive4j/derive4j) extension permits automatic derivation of [FunctionalJava](https://github.com/functionaljava/functionaljava) instances for the following type classes:

 - [Equal](https://github.com/functionaljava/functionaljava/blob/master/core/src/main/java/fj/Equal.java)
 - [Hash](https://github.com/functionaljava/functionaljava/blob/master/core/src/main/java/fj/Hash.java)
 - [Show](https://github.com/functionaljava/functionaljava/blob/master/core/src/main/java/fj/Show.java)
 - [Ord](https://github.com/functionaljava/functionaljava/blob/master/core/src/main/java/fj/Ord.java)

(generation of optics is planned).

So, now, no excuse to write [unsafe code that breaks parametricity](https://github.com/derive4j/derive4j/issues/50)! ;)

# Example Usage:
```java

import fj.*;
import org.derive4j.*;

@Data(flavour = Flavour.FJ)
@Derive(@Instances({ Show.class, Hash.class, Equal.class, Ord.class}))
public abstract class Either<A, B> {

  /**
   * The catamorphism for either. Folds over this either breaking into left or right.
   *
   * @param left The function to call if this is left.
   * @param right The function to call if this is right.
   * @return The reduced value.
   */
  public abstract <X> X either(F<A, X> left, F<B, X> right);


  // In case you need to interact with unsafe code that
  // expects hashCode/equal/toString to be implemented:

  @Deprecated
  @Override
  public final boolean equals(Object obj) {
    return Equal.equals0(Either.class, this, obj,
        Eithers.eitherEqual(Equal.anyEqual(), Equal.anyEqual()));
  }

  @Deprecated
  @Override
  public final int hashCode() {
    return Eithers.<A, B>eitherHash(Hash.anyHash(), Hash.anyHash()).hash(this);
  }

  @Deprecated
  @Override
  public final String toString() {
    return Eithers.<A, B>eitherShow(Show.anyShow(), Show.anyShow()).showS(this);
  }
}
```
Derive4J, through this extension will then derive the following code in the generated `Eithers.java` file:
```java
  
  public static <A, B> Show<Either<A, B>> eitherShow(Show<A> aShow, Show<B> bShow) {
    return Show.show(either -> either.either(
      (left) -> Stream.fromString("left(").append(() -> aShow.show(left)).append(Stream.fromString(")")),
      (right) -> Stream.fromString("right(").append(() -> bShow.show(right)).append(Stream.fromString(")"))
    ));
  }

  public static <A, B> Ord<Either<A, B>> eitherOrd(Ord<A> aOrd, Ord<B> bOrd) {
    return Ord.ordDef(either1 -> either1.either(
      (left1) -> either2 -> either2.either(
        (left2) -> {
          Ordering o = Ordering.EQ;
          o = aOrd.compare(left1, left2);
          if (o != Ordering.EQ) return o;
          return o;
        },
        (right2) -> Ordering.LT
      ),
      (right1) -> either2 -> either2.either(
        (left2) -> Ordering.GT,
        (right2) -> {
          Ordering o = Ordering.EQ;
          o = bOrd.compare(right1, right2);
          if (o != Ordering.EQ) return o;
          return o;
        }
      )
    ));
  }

  public static <A, B> Equal<Either<A, B>> eitherEqual(Equal<A> aEqual, Equal<B> bEqual) {
    return Equal.equalDef(either1 -> either1.either(
      (left1) -> either2 -> either2.either(
        (left2) -> aEqual.eq(left1, left2),
        (right2) -> false
      ),
      (right1) -> either2 -> either2.either(
        (left2) -> false,
        (right2) -> bEqual.eq(right1, right2)
      )
    ));
  }

  public static <A, B> Hash<Either<A, B>> eitherHash(Hash<A> aHash, Hash<B> bHash) {
    return Hash.hash(either -> either.either(
      (left) -> 23 + aHash.hash(left),
      (right) -> 29 + bHash.hash(right)
    ));
}
```

# Use it in your project
Derive4J-FJ is a "plugin" of [Derive4J](https://github.com/derive4j/derive4j) and should be declared (as well) as a apt/compile-time only
dependency (not needed at runtime). So while derive4j and derive4j-fj are (L)GPL-licensed, the generated code is not linked to derive4j, and thus __derive4j can be used in any project (proprietary or not)__.
Also you will need jdk8 version of FunctionalJava artifacts (4.7+).

## Maven:
```xml
<dependency>
    <groupId>org.functionaljava</groupId>
    <artifactId>functionaljava_1.8</artifactId>
    <version>4.7</version>
</dependency>
<dependency>
  <groupId>org.derive4j</groupId>
  <artifactId>derive4j-fj</artifactId>
  <version>0.1</version>
  <optional>true</optional>
</dependency>
<dependency>
  <groupId>org.derive4j</groupId>
  <artifactId>derive4j</artifactId>
  <version>0.12.1</version>
  <optional>true</optional>
</dependency>
```
[search.maven]: http://search.maven.org/#search|ga|1|org.derive4j.derive4j-fj

## Gradle
```
compile "org.functionaljava:functionaljava_1.8:4.7"
compile(group: 'org.derive4j', name: 'derive4j', version: '0.12.1', ext: 'jar')
compile(group: 'org.derive4j', name: 'derive4j-fj', version: '0.1', ext: 'jar')
```
or better using the [gradle-apt-plugin](https://github.com/tbroyer/gradle-apt-plugin):
```
compile "org.functionaljava:functionaljava_1.8:4.7"
compileOnly "org.derive4j:derive4j-annotation:0.12.1"
apt "org.derive4j:derive4j:0.12.1"
apt "org.derive4j:derive4j-fj:0.1"
```

## Contributing

Bug reports, feature requests and pull requests are welcome, as well as contributions to improve documentation.

## Contact
jb@giraudeau.info, [@jb9i](https://twitter.com/jb9i) or use the project GitHub issues.