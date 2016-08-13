package simpkins.dbquery;

import simpkins.query.Query;
import simpkins.query.QueryList;
import simpkins.query.QuerySet;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.type.*;

import java.math.BigDecimal;
import java.util.function.Function;

@SuppressWarnings("UnusedDeclaration")
public class EntityCustomExpression<TEntity, TValue> implements EntityExpression<TEntity, TValue> {
    private Class<TEntity> entityType;
    private Class<TValue> type;
    private String customSql;
    private Object[] components;
    private QueryList<EntityExpression> entityExpressions;
    private String key;

    public EntityCustomExpression(String customSql, EntityExpression<TEntity, TValue> expression) {
        this.entityType = expression.getEntityType();
        this.type = expression.getType();
        this.customSql = customSql;
        this.components = new Object[] { expression };
        this.entityExpressions = Query.from(components).ofType(EntityExpression.class).toList();
    }

    public EntityCustomExpression(String customSql, EntityExpression<TEntity, ?>... expressions) {
        this(null, customSql, expressions);
    }

    public EntityCustomExpression(Class<TValue> type, String customSql, EntityExpression<TEntity, ?>... expressions) {
        this.entityType = expressions.length > 0 ? expressions[0].getEntityType() : null;
        this.type = type;
        this.customSql = customSql;
        this.components = expressions;
        this.entityExpressions = Query.from(components).ofType(EntityExpression.class).toList();

        if (type == null) {
            QuerySet<Class> types = Query.from(expressions).select(x -> (Class)x.getType()).where(x -> x != null).toSet();
            if (types.size() == 1)
                //noinspection unchecked
                this.type = types.single();
        }
    }

    public EntityCustomExpression(String customSql, Object... components) {
        this(null, customSql, components);
    }

    public EntityCustomExpression(Class<TValue> type, String customSql, Object... components) {
        this.type = type;
        this.customSql = customSql;
        this.components = components;
        this.entityExpressions = Query.from(components).ofType(EntityExpression.class).toList();

        QuerySet<Class> entityTypes = entityExpressions.select(x -> x.getEntityType()).where(x -> x != null).toSet();
        if (entityTypes.size() > 1)
            throw new RuntimeException("Cannot use EntityExpressions of more than one entity type, found: " + entityTypes.size());
        //noinspection unchecked
        this.entityType = entityTypes.singleOrNull();

        if (type == null) {
            QuerySet<Class> types = Query.from(components).<Class>select(x -> x != null ? x instanceof EntityExpression ? ((EntityExpression)x).getType() : x.getClass() : null).where(x -> x != null).toSet();
            if (types.size() == 1)
                //noinspection unchecked
                this.type = types.single();
        }
    }

    public static <TEntity, TValue> EntityCustomExpression<TEntity, TValue> from(Object value) {
        return new EntityCustomExpression<>("%s", value);
    }

    @Override
    public Class<TEntity> getEntityType() {
        return entityType;
    }

    @Override
    public Class<TValue> getType() {
        return type;
    }

    @Override
    public boolean isCustom() {
        return true;
    }

    @Override
    public boolean isCollection() {
        return getEntityDefinitions().any(x -> x.isCollection());
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public String getEntityDefinitionFieldMapping() {
        throw new RuntimeException("EntityCustomExpression does not have an EntityDefinitionFieldMapping!");
    }

    @Override
    public EntityCustomExpression<TEntity, TValue> asEntityCustomExpression() {
        return this;
    }

    @Override
    public Query<EntityDefinition<TEntity, ?>> getEntityDefinitions() {
        return entityExpressions.selectMany(x -> x.getEntityDefinitions());
    }

    @Override
    public Query<Object> getNonEntityDefinitions() {
        return Query.from(components).selectMany(x -> x instanceof EntityExpression ? ((EntityExpression)x).getNonEntityDefinitions() : Query.from(x));
    }

    @Override
    public String toSql(Function<String, String> columnMapper, boolean unmasked) {
        return String.format(customSql, Query.from(components)
                .<String>select(x -> x instanceof EntityExpression ? ((EntityExpression) x).toSql(columnMapper, unmasked) : unmasked ? x != null ? x.toString() : "null" : "?")
                .toArray(String.class));
    }

    @Override
    public ColumnAggregate<TEntity, Long> count() {
        return new ColumnAggregate<>(ColumnAggregate.Type.COUNT, this, Long.class);
    }

    @Override
    public ColumnAggregate<TEntity, Long> countDistinct() {
        return new ColumnAggregate<>(ColumnAggregate.Type.COUNT_DISTINCT, this, Long.class);
    }

    @Override
    public ColumnAggregate<TEntity, TValue> sum() {
        return new ColumnAggregate<>(ColumnAggregate.Type.SUM, this, getType());
    }

    @Override
    public ColumnAggregate<TEntity, Double> avg() {
        return new ColumnAggregate<>(ColumnAggregate.Type.AVG, this, Double.class);
    }

    @Override
    public ColumnAggregate<TEntity, TValue> min() {
        return new ColumnAggregate<>(ColumnAggregate.Type.MIN, this, getType());
    }

    @Override
    public ColumnAggregate<TEntity, TValue> max() {
        return new ColumnAggregate<>(ColumnAggregate.Type.MAX, this, getType());
    }

    @Override
    public ColumnAggregate<TEntity, Boolean> any() {
        return new ColumnAggregate<>(ColumnAggregate.Type.ANY, this, Boolean.class);
    }

    @Override
    public ColumnAggregate<TEntity, Boolean> all() {
        return new ColumnAggregate<>(ColumnAggregate.Type.ALL, this, Boolean.class);
    }

    @Override
    public ColumnAggregate<TEntity, Boolean> none() {
        return new ColumnAggregate<>(ColumnAggregate.Type.NONE, this, Boolean.class);
    }

    @Override
    public ColumnAggregate<TEntity, TValue> groupBy() {
        return new ColumnAggregate<>(ColumnAggregate.Type.GROUP_BY, this, getType());
    }

    @Override
    public EntityExpression<TEntity, TValue> lower() {
        return new EntityCustomExpression<>(getType(), "lower(%s)", this);
    }

    @Override
    public EntityExpression<TEntity, TValue> upper() {
        return new EntityCustomExpression<>(getType(), "upper(%s)", this);
    }

    @Override
    public EntityExpression<TEntity, TValue> coalesce(Object... others) {
        return new EntityCustomExpression<>(getType(), "coalesce(" + Query.generate(others.length + 1, "%s").toString(", ") + ")", Query.from(others).insert(0, this).toArray());
    }

    @Override
    public String getKey() {
        if (key == null)
            key = String.format(customSql, Query.from(components).<String>select(x -> x instanceof EntityExpression ? ((EntityExpression) x).toSql(y -> y, true) : x != null ? x.toString() : "null").toArray(String.class));
        return key;
    }

    @Override
    public String toString() {
        return key;
    }

    protected HibernateSql toHibernateSql() {
        return new HibernateSql(this, false);
    }

    protected HibernateSql toHibernateSqlGrouped() {
        return new HibernateSql(this, true);
    }

    private class HibernateSql extends Order implements Criterion, Projection {
        EntityCustomExpression<?, ?> entityCustomExpression;
        String alias;
        Class type;
        boolean isGrouped;

        public HibernateSql(EntityCustomExpression<?, ?> entityCustomExpression, boolean isGrouped) {
            super(null, false);
            this.entityCustomExpression = entityCustomExpression;
            this.type = entityCustomExpression.getType();
            this.isGrouped = isGrouped;
        }

        @Override
        public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) {
            return toSql(x -> {
                String[] columns = criteriaQuery.findColumns(x, criteria);
                if (columns == null || columns.length != 1)
                    throw new RuntimeException("Cannot find column for " + x);
                return columns[0];
            }, false);
        }

        @Override
        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return entityCustomExpression.getNonEntityDefinitions()
                    .select(x -> new TypedValue(convertType(x != null ? x.getClass() : entityCustomExpression.getType()), x))
                    .toArray(TypedValue.class);
        }

        @Override
        public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
            alias = "y" + position + "_";
            return toSqlString(criteria, criteriaQuery) + " as " + alias;
        }

        @Override
        public String toGroupSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return toSqlString(criteria, criteriaQuery);
        }

        @Override
        public org.hibernate.type.Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return new org.hibernate.type.Type[] { convertType(type) };
        }

        @Override
        public org.hibernate.type.Type[] getTypes(String alias, Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return null;
        }

        @Override
        public String[] getColumnAliases(int position) {
            return new String[] { alias };
        }

        @Override
        public String[] getColumnAliases(String alias, int position) {
            return null;
        }

        @Override
        public String[] getAliases() {
            return new String[] { alias };
        }

        @Override
        public boolean isGrouped() {
            return isGrouped;
        }

        private org.hibernate.type.Type convertType(Class type) {
            if (type == null)
                return new StringType();
            // TODO make more complete/safe
            return type == boolean.class || type == Boolean.class ? new BooleanType() :
                    type == short.class || type == Short.class ? new ShortType() :
                    type == int.class || type == Integer.class ? new IntegerType() :
                    type == long.class || type == Long.class ? new LongType() :
                    type == BigDecimal.class ? new BigDecimalType() :
                    new StringType();
        }
    }
}
