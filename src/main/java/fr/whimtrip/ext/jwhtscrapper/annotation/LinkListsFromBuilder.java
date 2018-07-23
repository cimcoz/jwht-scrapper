/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

/*
 * This code is licensed to WhimTrip©. For any question, please contact the author of the file.
 */

package fr.whimtrip.ext.jwhtscrapper.annotation;

import fr.whimtrip.ext.jwhtscrapper.intfr.LinkListFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by LOUISSTEIMBERG on 19/11/2017.
 */


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD })
public @interface LinkListsFromBuilder {

    Class<? extends LinkListFactory> value() default LinkListFactory.class;

    boolean editRequest() default true;

}