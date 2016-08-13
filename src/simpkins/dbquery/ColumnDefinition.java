package simpkins.dbquery;

public class ColumnDefinition<TEntity, TValue> extends ColumnBaseDefinition<TEntity, TValue> {
    private boolean isNullable;

    public ColumnDefinition(Class<TEntity> entityType, Class<TValue> columnType, String fieldMapping, boolean isNullable) {
        super(entityType, columnType, fieldMapping);
        this.isNullable = isNullable;
    }

    public ColumnUpdate<TEntity, TValue> setNull() {
        return new ColumnUpdate<>(this, (TValue)null);
    }

    public ColumnUpdate<TEntity, TValue> set(TValue value) {
        return new ColumnUpdate<>(this, value);
    }

    public ColumnUpdate<TEntity, TValue> set(EntityExpression<TEntity, TValue> expression) {
        return new ColumnUpdate<>(this, expression);
    }

    @Override
    public boolean isNullable() {
        return isNullable;
    }
}
