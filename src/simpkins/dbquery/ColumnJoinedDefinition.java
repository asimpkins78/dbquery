package simpkins.dbquery;

import simpkins.query.QueryList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ColumnJoinedDefinition<TEntity, TValue> extends ColumnBaseDefinition<TEntity, TValue> {

    protected ColumnJoinedDefinition(Class<TEntity> entityType, Class<TValue> columnType, QueryList<EntityDefinition<?, ?>> fullPath) {
        super(entityType, columnType, fullPath);
    }

    @Override
    public TValue apply(TEntity entity) {
        try {
            Object current = entity;
            for (EntityDefinition<?, ?> step : getFullPath()) {
                Method getter = current.getClass().getDeclaredMethod(step.getGetterName());
                getter.setAccessible(true);
                current = getter.invoke(current);
                if (current == null)
                    return null;
            }
            //noinspection unchecked
            return (TValue)current;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isJoined() {
        return true;
    }

    @Override
    public boolean isNullable() {
        return getFullPath().any(x -> x.isNullable());
    }
}
