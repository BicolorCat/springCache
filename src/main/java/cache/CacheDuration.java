package cache;


import java.lang.annotation.*;

/**
 * Created by BiColorCat on 2017/2/3.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.METHOD})
public @interface CacheDuration {

    long duration() default 60;

}
