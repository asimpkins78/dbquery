package simpkins.dbquery;

import simpkins.query.QueryList;

public abstract class RelationBaseDefinition<TEntity, TValue> extends FieldDefinition<TEntity, TValue> implements EntityRelationship<TEntity, TValue> {

    protected RelationBaseDefinition(Class<TEntity> entityType, Class<TValue> columnType, String fieldMapping) {
        super(entityType, columnType, fieldMapping);
    }

    protected RelationBaseDefinition(Class<TEntity> entityType, Class<TValue> columnType, QueryList<EntityDefinition<?, ?>> fullPath) {
        super(entityType, columnType, fullPath);
    }

    public <TNextValue> ColumnJoinedDefinition<TEntity, TNextValue> join(ColumnBaseDefinition<TValue, TNextValue> target) {
        return new ColumnJoinedDefinition<>(getEntityType(), target.getColumnType(), getFullPath().combine(target.getFullPath()).toList());
    }

    public <TNextValue> RelationJoinedDefinition<TEntity, TNextValue> join(RelationBaseDefinition<TValue, TNextValue> target) {
        return new RelationJoinedDefinition<>(getEntityType(), target.getColumnType(), getFullPath().combine(target.getFullPath()).toList());
    }

    public <TNextValue> CollectionJoinedDefinition<TEntity, TNextValue> join(CollectionJoinedDefinition<TValue, TNextValue> target) {
        return new CollectionJoinedDefinition<>(getEntityType(), target.getColumnType(), getFullPath().combine(target.getFullPath()).toList());
    }

    public <TNextValue> CollectionRelationJoinedDefinition<TEntity, TNextValue> join(CollectionRelationBaseDefinition<TValue, TNextValue> target) {
        return new CollectionRelationJoinedDefinition<>(getEntityType(), target.getColumnType(), getFullPath().combine(target.getFullPath()).toList());
    }
}
