package simpkins.dbquery;

@SuppressWarnings("UnusedDeclaration")
public interface DbSubQueryMultipleResolution<TQuery, TEntity, TValue> {
    TQuery allEqualTo(TValue value);
    TQuery allEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery allGreaterThan(TValue value);
    TQuery allGreaterThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery allGreaterThanOrEqualTo(TValue value);
    TQuery allGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery allLessThan(TValue value);
    TQuery allLessThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery allLessThanOrEqualTo(TValue value);
    TQuery allLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyEqualTo(TValue value);
    TQuery anyEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyGreaterThan(TValue value);
    TQuery anyGreaterThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyGreaterThanOrEqualTo(TValue value);
    TQuery anyGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyLessThan(TValue value);
    TQuery anyLessThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyLessThanOrEqualTo(TValue value);
    TQuery anyLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);

    TQuery allNotEqualTo(TValue value);
    TQuery allNotEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery allNotGreaterThan(TValue value);
    TQuery allNotGreaterThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery allNotGreaterThanOrEqualTo(TValue value);
    TQuery allNotGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery allNotLessThan(TValue value);
    TQuery allNotLessThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery allNotLessThanOrEqualTo(TValue value);
    TQuery allNotLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyNotEqualTo(TValue value);
    TQuery anyNotEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyNotGreaterThan(TValue value);
    TQuery anyNotGreaterThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyNotGreaterThanOrEqualTo(TValue value);
    TQuery anyNotGreaterThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyNotLessThan(TValue value);
    TQuery anyNotLessThan(EntityExpression<TEntity, ?>... entityExpressions);
    TQuery anyNotLessThanOrEqualTo(TValue value);
    TQuery anyNotLessThanOrEqualTo(EntityExpression<TEntity, ?>... entityExpressions);
}
