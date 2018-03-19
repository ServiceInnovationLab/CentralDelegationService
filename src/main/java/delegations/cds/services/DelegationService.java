package delegations.cds.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;

import delegations.cds.models.Delegation;
import delegations.cds.views.DelegationView;
import delegations.cds.views.DelegationsView;
import delegations.cds.views.ImmutableDelegationsView;

@RequestScoped
public class DelegationService extends AbstractService {

    public DelegationsView findAll() {

        List<Delegation> delegations = query()
                .select(qDelegation)
                .from(qDelegation)
                .where(isActive(qDelegation), visibleToClient(qDelegation))
                .orderBy(qDelegation.id.asc())
                .fetch();

        List<DelegationView> views = delegations.stream()
                .map(Delegation::buildPublicView)
                .collect(Collectors.toList());

        return ImmutableDelegationsView.builder().delegations(views).build();
    }

    public DelegationView findByCrn(final String crn) {

        return query()
                .select(qDelegation)
                .from(qDelegation)
                .where(isActive(qDelegation), visibleToClient(qDelegation), qDelegation.crn.eq(crn))
                .fetchOne()
                .buildPublicView();
    }

    public Delegation findByInviteCode(final String inviteCode) {

        return query()
                .select(qDelegation)
                .from(qDelegation)
                .join(qDelegation.rendezvous, qRendezvous)
                .where(
                        isActive(qDelegation),
                        isActive(qRendezvous),
                        visibleToClient(qDelegation),
                        qRendezvous.ownerCode.eq(inviteCode).and(qRendezvous.ownerCodeConsumed.eq(false))
                                .or(qRendezvous.delegateCode.eq(inviteCode).and(qRendezvous.delegateCodeConsumed.eq(false))))
                .fetchOne();
    }

 }
