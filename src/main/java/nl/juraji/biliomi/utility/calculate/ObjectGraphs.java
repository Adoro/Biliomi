package nl.juraji.biliomi.utility.calculate;

import com.google.common.base.Preconditions;
import nl.juraji.biliomi.utility.estreams.EStream;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ClassUtils;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Juraji on 19-7-2017.
 * Biliomi v3
 */
public final class ObjectGraphs {
  private ObjectGraphs() {
    // Private constructor
  }

  /**
   * deep merge two maps
   *
   * @param target The target map
   * @param source The source map
   */
  public static <K, V> void mergeMaps(Map<K, V> target, Map<K, V> source) {
    source.forEach((sourceKey, sourceValue) -> {
      if (target.containsKey(sourceKey)) {
        Object targetValue = target.get(sourceKey);

        // The two objects are equal, so merging is not needed
        if (Objects.equals(targetValue, sourceValue)) {
          return;
        }

        // Merge collections
        // if the target is a collection, but the source is not an IllegalArgumentException is thrown
        if (targetValue instanceof Collection) {
          Preconditions.checkArgument(sourceValue instanceof Collection,
              "a non-collection collided with a collection: %s%n\t%s",
              sourceValue, targetValue);

          //noinspection unchecked
          ((Collection) targetValue).addAll((Collection) sourceValue);
          return;
        }

        // Merge maps
        // if the target is a map, but the source is not an IllegalArgumentException is thrown
        if (targetValue instanceof Map) {
          Preconditions.checkArgument(sourceValue instanceof Map,
              "a non-map collided with a map: %s%n\t%s",
              sourceValue, targetValue);

          //noinspection unchecked
          mergeMaps((Map<K, V>) targetValue, (Map<K, V>) sourceValue);
          return;
        }

        // Wait what?!
        // Will always throw IllegalArumentException when reached
        Preconditions.checkArgument(true, "collision detected: %s%n%\torig:%s", sourceValue, targetValue);
      } else {
        target.put(sourceKey, sourceValue);
      }
    });
  }

  /**
   * Deep merge to POJO objects
   *
   * @param target The target POJO
   * @param source The source POJO
   * @param <T>    The POJO type
   * @return The merge result
   */
  public static <T> T mergePojo(T target, T source) throws Exception {
    if (!target.getClass().equals(source.getClass())) {
      throw new IllegalArgumentException("Type mismatch " + source.getClass().getSimpleName() + " -> " + target.getClass().getSimpleName());
    }

    PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(target);

    EStream.from(descriptors).forEach(propertyDescriptor -> {

      Object targetValue = PropertyUtils.getProperty(target, propertyDescriptor.getName());
      Object sourceValue = PropertyUtils.getProperty(source, propertyDescriptor.getName());

      if (sourceValue != null) {
        if (targetValue == null) {
          PropertyUtils.setProperty(target, propertyDescriptor.getName(), sourceValue);
        } else {
          if (isJavaType(propertyDescriptor)) {
            if (targetValue != sourceValue) {
              PropertyUtils.setProperty(target, propertyDescriptor.getName(), sourceValue);
            }
          } else {
            mergePojo(targetValue, sourceValue);
          }
        }
      }
    });
    return target;
  }

  /**
   * Recursively initialize all non-primitive properties of an object
   *
   * @param o The object to initialize
   */
  public static void initializeObject(Object o) {
    PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(o);

    EStream.from(descriptors)
        .filter(propertyDescriptor -> !isJavaType(propertyDescriptor))
        .filter(propertyDescriptor -> PropertyUtils.getProperty(o, propertyDescriptor.getName()) == null)
        .forEach(propertyDescriptor -> {
          Class<?> type = propertyDescriptor.getPropertyType();
          Object subO = type.newInstance();
          PropertyUtils.setProperty(o, propertyDescriptor.getName(), subO);
          initializeObject(subO);
        });
  }

  /**
   * Check if the given property is a Java type
   * @param pd A PropertyDescriptor
   * @return True when the property type is a Java type, else False
   */
  public static boolean isJavaType(PropertyDescriptor pd) {
    return isJavaType(pd.getPropertyType());
  }

  /**
   * Check if the given class is a Java type
   * @param type A class
   * @return True when the property type is a Java type, else False
   */
  public static boolean isJavaType(Class<?> type) {
    return (ClassUtils.isPrimitiveOrWrapper(type)
        || Class.class.equals(type)
        || String.class.equals(type)
        || Collection.class.isAssignableFrom(type));
  }
}
