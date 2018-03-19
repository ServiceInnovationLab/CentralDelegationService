package delegations.cds.services;

import javax.enterprise.context.RequestScoped;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import delegations.cds.models.Template;


@RequestScoped
public class TemplateService extends AbstractService {

    public Template findById(final Long id) {

        return query()
                .select(qTemplate)
                .from(qTemplate)
                .where(visibleToClient(), qTemplate.id.eq(id))
                .fetchOne();
    }

    public BooleanExpression visibleToClient() {
        if (authContext.getClient() == null) {
            return Expressions.TRUE;
        }
        return qTemplate.client.eq(authContext.getClient());
    }


    public Template defaultTemplateForClient() {
       return query()
                .select(qTemplate)
                .from(qTemplate)
                .where(visibleToClient(), qTemplate.isDefault.isTrue())
                .fetchOne();
    }
}
