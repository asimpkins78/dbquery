package simpkins.dbquery;

import org.hibernate.NullPrecedence;
import org.hibernate.criterion.Order;

public class DbQueryOrder {
    public static <T> Order create(EntityExpression<T, ?> entityExpression, boolean isDescending, boolean isNullsFirst, boolean isIgnoreCase) {
        Order order;
        boolean isNullsReversed = isDescending != isNullsFirst;
        if (entityExpression.isCustom()) {
            order = new EntityCustomExpression<>(
                    (isIgnoreCase ? "lower(%s)" : "%s") +
                            (isDescending ? " desc" : " asc") +
                            (isNullsReversed ? isNullsFirst ? " nulls first" : " nulls last" : ""),
                    entityExpression).toHibernateSql();
        }
        else {
            order = isDescending
                    ? Order.desc(entityExpression.getEntityDefinitionFieldMapping())
                    : Order.asc(entityExpression.getEntityDefinitionFieldMapping());
            if (isIgnoreCase)
                order.ignoreCase();
            if (isNullsReversed)
                order.nulls(isNullsFirst ? NullPrecedence.FIRST : NullPrecedence.LAST);
        }
        return order;
    }
}
