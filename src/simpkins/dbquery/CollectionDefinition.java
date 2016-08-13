package simpkins.dbquery;

import simpkins.query.QueryList;

import java.util.Collection;
import java.util.function.Function;

public abstract class CollectionDefinition<TEntity, TValue> extends EntityDefinition<TEntity, TValue> implements Function<TEntity, Collection<TValue>> {

    protected CollectionDefinition(Class<TEntity> entityType, Class<TValue> columnType, String fieldMapping) {
        super(entityType, columnType, fieldMapping);
    }

    protected CollectionDefinition(Class<TEntity> entityType, Class<TValue> columnType, QueryList<EntityDefinition<?, ?>> fullPath) {
        super(entityType, columnType, fullPath);
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    @Override
    public boolean isNullable() {
        return true;
    }
}
