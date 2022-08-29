package nw4r;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;

/**
 * Describes an internally unused option.
 * 
 * @author Ogu99
 */
@Target({ TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR })
public @interface Unused { }