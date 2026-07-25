package jforgame.orm.core;

import jforgame.commons.util.TypeUtil;
import jforgame.orm.converter.ConverterFactory;
import jforgame.orm.converter.support.ObjectToJsonJpaConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Field metadata
 */
public class FieldMetaData {

    /**
     * Field reflection object
     */
    private Field field;

    /**
     * Field converter, used for conversion after reading and before writing
     */
    private AttributeConverter converter;

    public static FieldMetaData valueOf(Field field) {
        field.setAccessible(true);
        FieldMetaData metadata = new FieldMetaData();
        metadata.field = field;

        // If not primitive type or String, auto convert
        Class<?> type = metadata.getField().getType();
        if (!TypeUtil.isPrimitiveOrString(type)) {
            AttributeConverter convert = ConverterFactory.getAttributeConverter(ObjectToJsonJpaConverter.class);
            Convert annotation = field.getAnnotation(Convert.class);
            if (annotation != null) {
                convert = ConverterFactory.getAttributeConverter(annotation.converter());
            } else {
                // ObjectToJsonJpaConverter要求序列化后的json带上类的完整信息，因此不能是final类
                if (Modifier.isFinal(type.getModifiers())) {
                    throw new IllegalStateException(field.getName() + " with ObjectToJsonJpaConverter can not be used with final class");
                }
            }
            metadata.converter = convert;
        }
        return metadata;
    }

    public Field getField() {
        return field;
    }

    public AttributeConverter getConverter() {
        return converter;
    }
}
