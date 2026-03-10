package net.neoforged.neoforge.common.conditions;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ExtraCodecs;

/**
 * Utilities for condition-aware codecs.
 * <p>
 * In NeoForge this extends RegistryOps, but since vanilla RegistryOps
 * has a private constructor, we use a side-map approach to associate
 * condition contexts with ops instances.
 */
public final class ConditionalOps {
    private static final WeakHashMap<DynamicOps<?>, ICondition.IContext> CONTEXT_MAP = new WeakHashMap<>();

    private ConditionalOps() {}

    /**
     * Returns a MapCodec that retrieves an {@link ICondition.IContext} from ops.
     */
    public static MapCodec<ICondition.IContext> retrieveContext() {
        return ExtraCodecs.retrieveContext(ops -> {
            ICondition.IContext ctx = CONTEXT_MAP.get(ops);
            return DataResult.success(ctx != null ? ctx : ICondition.IContext.EMPTY);
        });
    }

    /** Key used for the conditions inside an object. */
    public static final String DEFAULT_CONDITIONS_KEY = "neoforge:conditions";
    /** Key used to store the value when the value is not a map. */
    public static final String CONDITIONAL_VALUE_KEY = "neoforge:value";

    /**
     * Creates a conditional ops wrapper by associating a context with the given ops.
     */
    public static <T> DynamicOps<T> create(DynamicOps<T> ops, ICondition.IContext context) {
        CONTEXT_MAP.put(ops, context);
        return ops;
    }

    public static <T> Codec<Optional<T>> createConditionalCodec(final Codec<T> ownerCodec) {
        return createConditionalCodec(ownerCodec, DEFAULT_CONDITIONS_KEY);
    }

    public static <T> Codec<Optional<T>> createConditionalCodec(final Codec<T> ownerCodec, String conditionalsKey) {
        return createConditionalCodecWithConditions(ownerCodec, conditionalsKey)
                .xmap(r -> r.map(WithConditions::carrier), r -> r.map(i -> new WithConditions<>(List.of(), i)));
    }

    public static <T> Codec<List<T>> decodeListWithElementConditions(final Codec<T> ownerCodec) {
        Codec<Optional<T>> conditional = createConditionalCodec(ownerCodec);
        return conditional.listOf().xmap(
                list -> list.stream().filter(Optional::isPresent).map(Optional::get).toList(),
                list -> list.stream().map(Optional::<T>of).toList()
        );
    }

    public static <T> Codec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(final Codec<T> ownerCodec) {
        return createConditionalCodecWithConditions(ownerCodec, DEFAULT_CONDITIONS_KEY);
    }

    public static <T> Codec<Optional<WithConditions<T>>> createConditionalCodecWithConditions(final Codec<T> ownerCodec, String conditionalsKey) {
        return Codec.of(
                new ConditionalEncoder<>(conditionalsKey, ICondition.LIST_CODEC, ownerCodec),
                new ConditionalDecoder<>(conditionalsKey, ICondition.LIST_CODEC, retrieveContext().codec(), ownerCodec));
    }

    private static final class ConditionalEncoder<A> implements Encoder<Optional<WithConditions<A>>> {
        private final String conditionalsPropertyKey;
        private final Codec<List<ICondition>> conditionsCodec;
        private final Encoder<A> innerCodec;

        private ConditionalEncoder(String conditionalsPropertyKey, Codec<List<ICondition>> conditionsCodec, Encoder<A> innerCodec) {
            this.conditionalsPropertyKey = conditionalsPropertyKey;
            this.conditionsCodec = conditionsCodec;
            this.innerCodec = innerCodec;
        }

        @Override
        public <T> DataResult<T> encode(Optional<WithConditions<A>> input, DynamicOps<T> ops, T prefix) {
            if (ops.compressMaps()) {
                return DataResult.error(() -> "Cannot use ConditionalCodec with compressing DynamicOps");
            }
            if (input.isEmpty()) {
                return DataResult.error(() -> "Cannot encode empty Optional with a ConditionalEncoder.");
            }

            final WithConditions<A> withConditions = input.get();
            if (withConditions.conditions().isEmpty()) {
                return innerCodec.encode(withConditions.carrier(), ops, prefix);
            }

            var recordBuilder = ops.mapBuilder();
            recordBuilder.add(conditionalsPropertyKey, conditionsCodec.encodeStart(ops, withConditions.conditions()));

            var encodedInner = innerCodec.encodeStart(ops, withConditions.carrier());
            return encodedInner.flatMap(inner -> ops.getMap(inner).map(innerMap -> {
                innerMap.entries().forEach(pair -> recordBuilder.add(pair.getFirst(), pair.getSecond()));
                return recordBuilder.build(prefix);
            }).result().orElseGet(() -> {
                recordBuilder.add(CONDITIONAL_VALUE_KEY, inner);
                return recordBuilder.build(prefix);
            }));
        }

        @Override
        public String toString() {
            return "Conditional[" + innerCodec + "]";
        }
    }

    @SuppressWarnings("unchecked")
    private static final class ConditionalDecoder<A> implements Decoder<Optional<WithConditions<A>>> {
        private final String conditionalsPropertyKey;
        private final Codec<List<ICondition>> conditionsCodec;
        private final Codec<ICondition.IContext> contextCodec;
        private final Decoder<A> innerCodec;

        private ConditionalDecoder(String conditionalsPropertyKey, Codec<List<ICondition>> conditionsCodec, Codec<ICondition.IContext> contextCodec, Decoder<A> innerCodec) {
            this.conditionalsPropertyKey = conditionalsPropertyKey;
            this.conditionsCodec = conditionsCodec;
            this.contextCodec = contextCodec;
            this.innerCodec = innerCodec;
        }

        @Override
        public <T> DataResult<Pair<Optional<WithConditions<A>>, T>> decode(DynamicOps<T> ops, T input) {
            if (ops.compressMaps()) {
                return DataResult.error(() -> "Cannot use ConditionalCodec with compressing DynamicOps");
            }

            return ops.getMap(input).<DataResult<Pair<Optional<WithConditions<A>>, T>>>map(inputMap -> {
                final T conditionsData = inputMap.get(conditionalsPropertyKey);
                if (conditionsData == null) {
                    DataResult<Pair<A, T>> dr = innerCodec.decode(ops, input);
                    return dr.map(result -> Pair.of(Optional.of(new WithConditions<>(result.getFirst())), result.getSecond()));
                }

                return conditionsCodec.decode(ops, conditionsData).flatMap(conditionsCarrier -> {
                    final List<ICondition> conditions = conditionsCarrier.getFirst();
                    return contextCodec.decode(ops, ops.emptyMap()).flatMap(contextCarrier -> {
                        final ICondition.IContext ctx = contextCarrier.getFirst();
                        if (!conditions.stream().allMatch(c -> c.test(ctx))) {
                            return DataResult.success(Pair.of(Optional.empty(), input));
                        }

                        T valueData = inputMap.get(CONDITIONAL_VALUE_KEY);
                        DataResult<Pair<A, T>> innerResult;
                        if (valueData != null) {
                            innerResult = innerCodec.decode(ops, valueData);
                        } else {
                            T condKey = ops.createString(conditionalsPropertyKey);
                            var mapForDecoding = ops.createMap(inputMap.entries()
                                    .filter(pair -> !pair.getFirst().equals(condKey)));
                            innerResult = innerCodec.decode(ops, mapForDecoding);
                        }

                        return innerResult.map(result -> Pair.of(
                                Optional.of(new WithConditions<>(conditions, result.getFirst())),
                                result.getSecond()));
                    });
                });
            }).result().orElseGet(() -> {
                DataResult<Pair<A, T>> dr = innerCodec.decode(ops, input);
                return dr.map(result -> Pair.of(Optional.of(new WithConditions<>(result.getFirst())), result.getSecond()));
            });
        }
    }
}
