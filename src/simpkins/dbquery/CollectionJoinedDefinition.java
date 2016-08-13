package simpkins.dbquery;

import simpkins.query.Query;
import simpkins.query.QueryList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("UnusedDeclaration")
public class CollectionJoinedDefinition<TEntity, TValue> extends CollectionDefinition<TEntity, TValue> implements EntityExpression<TEntity, TValue> {

    protected CollectionJoinedDefinition(Class<TEntity> entityType, Class<TValue> columnType, QueryList<EntityDefinition<?, ?>> fullPath) {
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
    public Class<TValue> getType() {
        return getColumnType();
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    @Override
    public String getEntityDefinitionFieldMapping() {
        return getFieldMapping();
    }

    @Override
    public EntityCustomExpression<TEntity, TValue> asEntityCustomExpression() {
        return EntityCustomExpression.from(this);
    }

    @Override
    public Query<EntityDefinition<TEntity, ?>> getEntityDefinitions() {
        return Query.from(this);
    }

    @Override
    public Query<Object> getNonEntityDefinitions() {
        return Query.empty();
    }

    @Override
    public String toSql(Function<String, String> columnMapper, boolean unmasked) {
        return columnMapper.apply(getFieldMapping());
    }

    @Override
    public ColumnAggregate<TEntity, Long> count() {
        return new ColumnAggregate<>(ColumnAggregate.Type.COUNT, this, Long.class);
    }

    @Override
    public ColumnAggregate<TEntity, Long> countDistinct() {
        return new ColumnAggregate<>(ColumnAggregate.Type.COUNT_DISTINCT, this, Long.class);
    }

    @Override
    public ColumnAggregate<TEntity, TValue> sum() {
        return new ColumnAggregate<>(ColumnAggregate.Type.SUM, this, getColumnType());
    }

    @Override
    public ColumnAggregate<TEntity, Double> avg() {
        return new ColumnAggregate<>(ColumnAggregate.Type.AVG, this, Double.class);
    }

    @Override
    public ColumnAggregate<TEntity, TValue> min() {
        return new ColumnAggregate<>(ColumnAggregate.Type.MIN, this, getColumnType());
    }

    @Override
    public ColumnAggregate<TEntity, TValue> max() {
        return new ColumnAggregate<>(ColumnAggregate.Type.MAX, this, getColumnType());
    }

    @Override
    public ColumnAggregate<TEntity, Boolean> any() {
        return new ColumnAggregate<>(ColumnAggregate.Type.ANY, this, Boolean.class);
    }

    @Override
    public ColumnAggregate<TEntity, Boolean> all() {
        return new ColumnAggregate<>(ColumnAggregate.Type.ALL, this, Boolean.class);
    }

    @Override
    public ColumnAggregate<TEntity, Boolean> none() {
        return new ColumnAggregate<>(ColumnAggregate.Type.NONE, this, Boolean.class);
    }

    @Override
    public ColumnAggregate<TEntity, TValue> groupBy() {
        return new ColumnAggregate<>(ColumnAggregate.Type.GROUP_BY, this, getColumnType());
    }

    @Override
    public EntityExpression<TEntity, TValue> lower() {
        return asEntityCustomExpression().lower();
    }

    @Override
    public EntityExpression<TEntity, TValue> upper() {
        return asEntityCustomExpression().upper();
    }

    @Override
    public EntityExpression<TEntity, TValue> coalesce(Object... others) {
        return asEntityCustomExpression().coalesce(others);
    }

    @Override
    public String getKey() {
        return getFieldMapping();
    }
}
