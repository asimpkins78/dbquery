package simpkins.dbquery;

public class ColumnUpdate<TEntity, TValue> {
    private ColumnDefinition<TEntity, TValue> columnDefinition;
    private TValue value;
    private EntityExpression<TEntity, TValue> valueExpression;

    public ColumnUpdate(ColumnDefinition<TEntity, TValue> columnDefinition, TValue value) {
        this.columnDefinition = columnDefinition;
        this.value = value;

        //TODO validation through reflection?
    }

    public ColumnUpdate(ColumnDefinition<TEntity, TValue> columnDefinition, EntityExpression<TEntity, TValue> valueExpression) {
        this.columnDefinition = columnDefinition;
        this.valueExpression = valueExpression;

        if (valueExpression.getEntityDefinitions().any(x -> x.isJoined()))
            throw new RuntimeException("Cannot build ColumnUpdate with expression that requires a join!");
    }

    public ColumnDefinition<TEntity, TValue> getColumnDefinition() {
        return columnDefinition;
    }

    public TValue getValue() {
        return value;
    }

    public boolean isValueExpression() {
        return valueExpression != null;
    }

    public EntityExpression<TEntity, TValue> getValueExpression() {
        return valueExpression;
    }
}
