package simpkins.dbquery;

import simpkins.query.Query;

import java.util.function.Function;

@SuppressWarnings("UnusedDeclaration")
public interface EntityExpression<TEntity, TValue> extends DbResultRowKey<TValue> {
    Class<TEntity> getEntityType();
    Class<TValue> getType();
    boolean isCustom();
    boolean isCollection();
    boolean isNullable();
    EntityCustomExpression<TEntity, TValue> asEntityCustomExpression();
    String getEntityDefinitionFieldMapping();
    Query<EntityDefinition<TEntity, ?>> getEntityDefinitions();
    Query<Object> getNonEntityDefinitions();
    String toSql(Function<String, String> columnMapper, boolean unmasked);

    ColumnAggregate<TEntity, Long> count();
    ColumnAggregate<TEntity, Long> countDistinct();
    ColumnAggregate<TEntity, TValue> sum();
    ColumnAggregate<TEntity, Double> avg();
    ColumnAggregate<TEntity, TValue> min();
    ColumnAggregate<TEntity, TValue> max();
    ColumnAggregate<TEntity, Boolean> any();
    ColumnAggregate<TEntity, Boolean> all();
    ColumnAggregate<TEntity, Boolean> none();
    ColumnAggregate<TEntity, TValue> groupBy();
    EntityExpression<TEntity, TValue> lower();
    EntityExpression<TEntity, TValue> upper();
    EntityExpression<TEntity, TValue> coalesce(Object... others);
}
