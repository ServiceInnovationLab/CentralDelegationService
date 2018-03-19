package delegations.cds.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;

import delegations.cds.models.QUser;
import delegations.cds.models.User;
import delegations.cds.views.ImmutableUsersView;
import delegations.cds.views.UserView;
import delegations.cds.views.UsersView;

@RequestScoped
public class UserService {

    @PersistenceContext
    private EntityManager em;

    public UsersView findAll(final String filter) {
        QUser user = QUser.user;

        if (filter != null) {
            ServiceResponse.forException(new UnsupportedOperationException("TODO"));
        }

        List<User> users = query()
                .select(user)
                .from(user)
                .where(isActive())
                .fetch();


        List<UserView> uviews = users.stream()
                .map(User::buildPublicView)
                .collect(Collectors.toList());

        return ImmutableUsersView.builder().users(uviews).build();
    }

    public User save(final User user) {
        return em.merge(user);
    }

    /**
     *  Query Helpers
     **/
    public BooleanExpression isActive() {
        return QUser.user.deleteTime.isNull();
    }

    public JPAQuery<Void> query() {
        return new JPAQuery<>(em);
    }
}
