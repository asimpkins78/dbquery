package simpkins.dbquery;

import simpkins.query.QueryList;

public abstract class EntityDefinition<TEntity, TValue> {
    private Class<TEntity> entityType;
    private Class<TValue> columnType;
    private String fieldMapping;
    private QueryList<EntityDefinition<?, ?>> fullPath;

    protected EntityDefinition(Class<TEntity> entityType, Class<TValue> columnType, String fieldMapping) {
        this.entityType = entityType;
        this.columnType = columnType;
        this.fieldMapping = fieldMapping;
        this.fullPath = QueryList.of(this);
    }

    protected EntityDefinition(Class<TEntity> entityType, Class<TValue> columnType, QueryList<EntityDefinition<?, ?>> fullPath) {
        if (fullPath.size() < 2)
            throw new RuntimeException("Cannot create a joined EntityDefinition with no joins.");

        this.entityType = entityType;
        this.columnType = columnType;
        this.fieldMapping = fullPath.take(fullPath.size() - 1).select(x -> x.getFieldMapping()).toString("_") + "." + fullPath.last().getFieldMapping();
        this.fullPath = fullPath;
    }

    public Class<TEntity> getEntityType() {
        return entityType;
    }

    public Class<TValue> getColumnType() {
        return columnType;
    }

    public String getFieldMapping() {
        return fieldMapping;
    }

    public QueryList<EntityDefinition<?, ?>> getFullPath() {
        return fullPath;
    }

    // copy/pasted from AbstractGenerator.createGetterName()
    protected String getGetterName() {
        String getterRoot = getFieldMapping().substring(0, 1).toUpperCase() + getFieldMapping().substring(1);
        if (columnType.getSimpleName().equalsIgnoreCase("boolean")) {
            if (getterRoot.startsWith("Is"))
                return "i" + getterRoot.substring(1);
            if (getterRoot.startsWith("Can") && !Character.isLowerCase(getterRoot.charAt(3)))
                return "c" + getterRoot.substring(1);
        }
        return "get" + getterRoot;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isJoined() {
        return false;
    }

    public abstract boolean isNullable();

    @Override
    public String toString() {
        return getFieldMapping();
    }
}
