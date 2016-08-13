package simpkins.dbquery;

public class ColumnAggregate<TEntity, TValue> implements DbResultRowKey<TValue> {
    enum Type { COUNT, COUNT_DISTINCT, SUM, AVG, MIN, MAX, ANY, ALL, NONE, GROUP_BY }
    private Type type;
    private EntityExpression<TEntity, ?> entityExpression;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private Class<TValue> valueType;

    public ColumnAggregate(Type type, EntityExpression<TEntity, ?> entityExpression, Class<TValue> valueType) {
        this.type = type;
        this.entityExpression = entityExpression;
        this.valueType = valueType;
    }

    public Type getType() {
        return type;
    }

    public EntityExpression<TEntity, ?> getEntityExpression() {
        return entityExpression;
    }

    @Override
    public String getKey() {
        switch (type) {
            case COUNT:
                return entityExpression.getKey() + "__count";
            case COUNT_DISTINCT:
                return entityExpression.getKey() + "__count_distinct";
            case SUM:
                return entityExpression.getKey() + "__sum";
            case AVG:
                return entityExpression.getKey() + "__avg";
            case MIN:
                return entityExpression.getKey() + "__min";
            case MAX:
                return entityExpression.getKey() + "__max";
            case ANY:
                return entityExpression.getKey() + "__any";
            case ALL:
                return entityExpression.getKey() + "__all";
            case NONE:
                return entityExpression.getKey() + "__none";
            case GROUP_BY:
                return entityExpression.getKey();
            default:
                throw new RuntimeException("Unsupported type: " + type);
        }
    }
}
