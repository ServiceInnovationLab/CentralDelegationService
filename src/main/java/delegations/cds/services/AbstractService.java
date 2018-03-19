package delegations.cds.services;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.slf4j.Logger;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;

import delegations.cds.auth.AuthenticatedContext;
import delegations.cds.models.BaseModel;
import delegations.cds.models.QDelegation;
import delegations.cds.models.QDelegationType;
import delegations.cds.models.QPolicy;
import delegations.cds.models.QRendezvous;
import delegations.cds.models.QResource;
import delegations.cds.models.QTemplate;
import delegations.cds.models.QUser;
import io.opentracing.Tracer;

public abstract class AbstractService {

    static QResource qResource = QResource.resource;
    static QUser qUser = QUser.user;
    static QDelegation qDelegation = QDelegation.delegation;
    static QDelegationType qDelegationType = QDelegationType.delegationType;
    static QRendezvous qRendezvous = QRendezvous.rendezvous;
    static QTemplate qTemplate = QTemplate.template;
    static QPolicy pPolicy = QPolicy.policy;

    @Inject
    Logger logger;

    @Inject
    Tracer tracer;

    @PersistenceContext
    EntityManager em;

    AuthenticatedContext authContext;

    @Inject
    void setAuthContext(AuthenticatedContext authContext) {
        this.authContext = authContext;
    }

    <I, T extends BaseModel<I>> ServiceResponse<T> save(final T entity) {

        try {
            T m = em.merge(entity);
            em.flush();
            ServiceResponse.Success<T> sr = ServiceResponse.forSuccess(m);
            sr.setId(m.getExternalId());
            return sr;

        } catch (PersistenceException e) {
            logger.error("Failed to persist");
            return ServiceResponse.forException(e);
        }
    }

    BooleanExpression isActive(final QResource model) {
        return model.deleteTime.isNull();
    }

    BooleanExpression isActive(final QUser model) {
        return model.deleteTime.isNull();
    }

    BooleanExpression isActive(final QDelegation model) {
        return model.deleteTime.isNull();
    }

    BooleanExpression isActive(final QRendezvous model) {
        return model.deleteTime.isNull();
    }

    BooleanExpression isActive(final QDelegationType model) {
        return model.deleteTime.isNull();
    }

    BooleanExpression isActive(final QTemplate model) {
        return model.deleteTime.isNull();
    }

    BooleanExpression visibleToClient(final QResource queryType) {
        if (authContext.getClient() == null) {
            return Expressions.TRUE;
        }
        return queryType.client.eq(authContext.getClient());
    }

    BooleanExpression visibleToClient(final QDelegation queryType) {
        if (authContext.getClient() == null) {
            return Expressions.TRUE;
        }
        return queryType.client.eq(authContext.getClient());
    }

    BooleanExpression visibleToClient(final QDelegationType queryType) {
        if (authContext.getClient() == null) {
            return Expressions.TRUE;
        }
        return queryType.client.eq(authContext.getClient());
    }

    BooleanExpression visibleToClient(final QTemplate queryType) {
        if (authContext.getClient() == null) {
            return Expressions.TRUE;
        }
        return queryType.client.eq(authContext.getClient());
    }

    <T> JPAQuery<T> query() {
        return new JPAQuery<>(em);
    }


}
