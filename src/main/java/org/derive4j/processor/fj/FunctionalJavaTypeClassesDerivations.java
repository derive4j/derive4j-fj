/*
 * Copyright (c) 2017, Jean-Baptiste Giraudeau <jb@giraudeau.info>
 *
 * This file is part of "Derive4J - Functional Java".
 *
 * "Derive4J - Functional Java" is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * "Derive4J - Functional Java" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Derive4J - Functional Java".  If not, see <http://www.gnu.org/licenses/>.
 */
package org.derive4j.processor.fj;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.derive4j.processor.api.DerivatorFactory;
import org.derive4j.processor.api.DerivatorSelection;
import org.derive4j.processor.api.DeriveUtils;
import org.derive4j.processor.api.model.DataArgument;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.derive4j.processor.api.DerivatorSelections.selection;

@AutoService(DerivatorFactory.class)
public final class FunctionalJavaTypeClassesDerivations implements DerivatorFactory {

  private static final List<Integer> PRIMES = asList(23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
      101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223,
      227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353,
      359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461, 463, 467, 479, 487, 491,
      499, 503, 509, 521, 523, 541, 547, 557, 563, 569, 571, 577, 587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643,
      647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809,
      811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967,
      971, 977, 983, 991, 997);

  @Override
  public List<DerivatorSelection> derivators(DeriveUtils deriveUtils) {

    final ClassName showClass = ClassName.get("fj", "Show");
    final ClassName hashClass = ClassName.get("fj", "Hash");
    final ClassName equalClass = ClassName.get("fj", "Equal");
    final ClassName ordClass = ClassName.get("fj", "Ord");
    final ClassName ordering = ClassName.get("fj", "Ordering");
    final ClassName streamClass = ClassName.get("fj.data", "Stream");

    return asList(

        selection(showClass, adt -> deriveUtils.generateInstance(adt, showClass, emptyList(), instanceUtils ->

        instanceUtils.generateInstanceFactory(CodeBlock.builder()
            .add("$1T.show($2L -> $2L.", showClass, instanceUtils.adtVariableName())
            .add(instanceUtils.matchImpl(constructor -> deriveUtils.lambdaImpl(constructor,
                CodeBlock.builder()
                    .add("$T.fromString($S).append(() -> ", streamClass, constructor.name() + "(")
                    .add(constructor.arguments()
                        .stream()
                        .map(da -> instanceUtils.instanceFor(da)
                            .toBuilder()
                            .add(".show($L)", castWildcardedArg(da, "", deriveUtils))
                            .build())
                        .reduce((cb1, cb2) -> cb1.toBuilder()
                            .add(".append($T.fromString($S)).append(() -> ", streamClass, ", ")
                            .add(cb2)
                            .add(")")
                            .build())
                        .orElse(CodeBlock.of("$T.nil()", streamClass)))
                    .add(").append($T.fromString($S))", streamClass, ")")
                    .build())))
            .add(")")
            .build())

        )),

        selection(hashClass, adt -> deriveUtils.generateInstance(adt, hashClass, emptyList(), instanceUtils ->

        instanceUtils.generateInstanceFactory(CodeBlock.builder()
            .add("$1T.hash($2L -> $2L.", hashClass, instanceUtils.adtVariableName())
            .add(instanceUtils.matchImpl(constructor -> {

              String primeForThisConstructor = PRIMES.get(constructor.index()).toString();
              return deriveUtils.lambdaImpl(constructor,
                  CodeBlock.builder()
                      .add("$L",
                          IntStream.range(0, constructor.arguments().size() - 1).mapToObj(__ -> "(").collect(
                              Collectors.joining()))
                      .add(primeForThisConstructor)
                      .add(constructor.arguments()
                          .stream()
                          .map(da -> CodeBlock.builder()
                              .add(" + ")
                              .add(instanceUtils.instanceFor(da))
                              .add(".hash($L)", castWildcardedArg(da, "", deriveUtils))
                              .build())
                          .reduce((cb1, cb2) -> cb1.toBuilder().add(") * " + primeForThisConstructor).add(cb2).build())
                          .orElse(CodeBlock.of("")))
                      .build());
            }))
            .add(")")
            .build())

        )),

        selection(equalClass, adt -> deriveUtils.generateInstance(adt, equalClass, emptyList(), instanceUtils ->

        instanceUtils.generateInstanceFactory(CodeBlock.builder()
            .add("$1T.equalDef($2L -> $2L.", equalClass, instanceUtils.adtVariableName() + 1)
            .add(instanceUtils.matchImpl(constructor -> {

              String adt2 = instanceUtils.adtVariableName() + 2;

              return deriveUtils.lambdaImpl(constructor, "1",
                  CodeBlock.builder()
                      .add("$1L -> $1L.", adt2)
                      .add(instanceUtils.matchImpl(constructor2 -> deriveUtils.lambdaImpl(constructor2, "2",
                          constructor.name().equals(constructor2.name())
                              ? constructor.arguments()
                                  .stream()
                                  .map(da -> CodeBlock.builder()
                                      .add(instanceUtils.instanceFor(da))
                                      .add(".eq($L, $L)", castWildcardedArg(da, "1", deriveUtils),
                                          castWildcardedArg(da, "2", deriveUtils))
                                      .build())
                                  .reduce((cb1, cb2) -> cb1.toBuilder().add(" && ").add(cb2).build())
                                  .orElse(CodeBlock.of("true"))
                              : CodeBlock.of("false"))))
                      .build());
            }))
            .add(")")
            .build())

        )),

        selection(ordClass, adt -> deriveUtils.generateInstance(adt, ordClass, emptyList(), instanceUtils ->

        instanceUtils.generateInstanceFactory(CodeBlock.builder()
            .add("$1T.ordDef($2L -> $2L.", ordClass, instanceUtils.adtVariableName() + 1)
            .add(instanceUtils.matchImpl(constructor -> {

              String adt2 = instanceUtils.adtVariableName() + 2;

              return deriveUtils.lambdaImpl(constructor, "1", CodeBlock.builder()
                  .add("$1L -> $1L.", adt2)
                  .add(instanceUtils.matchImpl(constructor2 -> deriveUtils.lambdaImpl(constructor2, "2",
                      constructor.name().equals(constructor2.name())
                          ? CodeBlock.builder()
                              .add("{\n")
                              .indent()
                              .addStatement("$1T o = $1T.EQ", ordering)
                              .add(constructor.arguments()
                                  .stream()
                                  .map(da -> CodeBlock.builder()
                                      .add("o = ")
                                      .add(instanceUtils.instanceFor(da))
                                      .add(".compare($L, $L);\n", castWildcardedArg(da, "1", deriveUtils),
                                          castWildcardedArg(da, "2", deriveUtils))
                                      .addStatement("if (o != $T.EQ) return o", ordering)
                                      .build())
                                  .reduce((cb1, cb2) -> cb1.toBuilder().add(cb2).build())
                                  .orElse(CodeBlock.of("")))
                              .addStatement("return o")
                              .unindent()
                              .add("}")
                              .build()
                          : CodeBlock.of("$T.$L", ordering, constructor.index() < constructor2.index() ? "LT" : "GT"))))
                  .build());
            }))
            .add(")")
            .build()))));
  }

  private static CodeBlock castWildcardedArg(DataArgument da, String argSuffix, DeriveUtils deriveUtils) {
    return deriveUtils.isWildcarded(da.type())

        ? CodeBlock.of("($T) $L", deriveUtils.types().erasure(da.type()), da.fieldName() + argSuffix)

        : CodeBlock.of("$L", da.fieldName() + argSuffix);
  }
}
