package net.neoforged.fml.common;

import net.neoforged.api.distmarker.Dist;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface EventBusSubscriber {
        String modid() default "";
        Bus bus() default Bus.FORGE;
        Dist[] value() default {};

        enum Bus {
            FORGE, MOD
        }
    }
}
