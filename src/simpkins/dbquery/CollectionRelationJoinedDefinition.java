package simpkins.dbquery;

import simpkins.query.QueryList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionRelationJoinedDefinition<TEntity, TValue> extends CollectionRelationBaseDefinition<TEntity, TValue> {

    protected CollectionRelationJoinedDefinition(Class<TEntity> entityType, Class<TValue> columnType, QueryList<EntityDefinition<?, ?>> fullPath) {
        super(entityType, columnType, fullPath);
    }

    @Override
    public Collection<TValue> apply(TEntity entity) {
        try {
            boolean isCollection = false;
            Object current = entity;
            Class<?> currentClass = entity.getClass();
            for (EntityDefinition<?, ?> step : getFullPath()) {
                Method getter = currentClass.getDeclaredMethod(step.getGetterName());
                getter.setAccessible(true);
                if (isCollection) {
                    List<Object> list = new ArrayList<>();
                    for (Object item : (Collection)current) {
                        Object conversion = getter.invoke(item);
                        if (step.isCollection()) {
                            for (Object conversionItem : (Collection)conversion)
                                if (conversionItem != null)
                                    list.add(conversionItem);
                        }
                        else if (conversion != null)
                            list.add(conversion);
                    }
                    current = list;
                }
                else {
                    isCollection = step.isCollection();
                    current = getter.invoke(current);
                    if (current == null)
                        return null;
                }
                currentClass = step.getColumnType();
            }
            //noinspection unchecked
            return (Collection<TValue>)current;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isJoined() {
        return true;
    }

    @Override
    public String getFkSourceFieldMapping() {
        throw new RuntimeException("getFkSourceFieldMapping() not supported for CollectionRelationJoinedDefinition!");
    }

    @Override
    public String getFkTargetFieldMapping() {
        throw new RuntimeException("getFkTargetFieldMapping() not supported for CollectionRelationJoinedDefinition!");
    }
}
