package simpkins.dbquery;

import simpkins.query.QueryList;

public interface EntityRelationship<TEntity, TValue> {
    Class<TEntity> getEntityType();
    Class<TValue> getColumnType();
    String getFieldMapping();
    QueryList<EntityDefinition<?, ?>> getFullPath();
    boolean isCollection();
    boolean isJoined();
    String getFkSourceFieldMapping();
    String getFkTargetFieldMapping();
}
