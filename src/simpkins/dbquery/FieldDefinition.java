package simpkins.dbquery;

import simpkins.query.QueryList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

@SuppressWarnings("UnusedDeclaration")
public abstract class FieldDefinition<TEntity, TValue> extends EntityDefinition<TEntity, TValue> implements Function<TEntity, TValue> {

    protected FieldDefinition(Class<TEntity> entityType, Class<TValue> columnType, String fieldMapping) {
        super(entityType, columnType, fieldMapping);
    }

    protected FieldDefinition(Class<TEntity> entityType, Class<TValue> columnType, QueryList<EntityDefinition<?, ?>> fullPath) {
        super(entityType, columnType, fullPath);
    }

    @Override
    public TValue apply(TEntity entity) {
        try {
            Method getter = entity.getClass().getDeclaredMethod(getGetterName());
            getter.setAccessible(true);
            //noinspection unchecked
            return (TValue)getter.invoke(entity);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
