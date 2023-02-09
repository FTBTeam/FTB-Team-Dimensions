package dev.ftb.mods.ftbdimensions.portal;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Identifies a method that you should use but will likely look like it's deprecated as forge uses the annotation wrong.
 * This will typically be paired with a {@link SuppressWarnings} annotation as well.
 * <p>
 * TODO: Move this to FTB Lib
 */
@Target({METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface ForgeOnlyOverride {
}
