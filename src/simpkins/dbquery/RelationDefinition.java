package simpkins.dbquery;

public class RelationDefinition<TEntity, TValue> extends RelationBaseDefinition<TEntity, TValue> {
    private boolean isNullable;
    private String fkSourceFieldMapping;
    private String fkTargetFieldMapping;

    public RelationDefinition(Class<TEntity> entityType, Class<TValue> columnType, String fieldMapping, String fkSourceFieldMapping, String fkTargetFieldMapping, boolean isNullable) {
        super(entityType, columnType, fieldMapping);
        this.fkSourceFieldMapping = fkSourceFieldMapping;
        this.fkTargetFieldMapping = fkTargetFieldMapping;
        this.isNullable = isNullable;
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
    public boolean isNullable() {
        return isNullable;
    }
}
