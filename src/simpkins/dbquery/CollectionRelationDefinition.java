package simpkins.dbquery;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class CollectionRelationDefinition<TEntity, TValue> extends CollectionRelationBaseDefinition<TEntity, TValue> {
    private String fkSourceFieldMapping;
    private String fkTargetFieldMapping;

    public CollectionRelationDefinition(Class<TEntity> entityType, Class<TValue> columnType, String fieldMapping, String fkSourceFieldMapping, String fkTargetFieldMapping) {
        super(entityType, columnType, fieldMapping);
        this.fkSourceFieldMapping = fkSourceFieldMapping;
        this.fkTargetFieldMapping = fkTargetFieldMapping;
    }

    @Override
    public String getFkSourceFieldMapping() {
        return fkSourceFieldMapping;
    }

    @Override
    public String getFkTargetFieldMapping() {
        return fkTargetFieldMapping;
    }

    @Override
    public Collection<TValue> apply(TEntity entity) {
        try {
            //noinspection unchecked
            return (Collection<TValue>)entity.getClass().getDeclaredMethod(getGetterName()).invoke(entity);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
