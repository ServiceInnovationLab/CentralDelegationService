package delegations.cds.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.transaction.Transactional;

import delegations.cds.models.DelegationType;
import delegations.cds.models.Template;
import delegations.cds.views.DelegationTypeView;
import delegations.cds.views.DelegationTypesView;
import delegations.cds.views.ImmutableDelegationTypesView;

@RequestScoped
public class DelegationTypeService extends AbstractService {

    public DelegationTypesView findAll(final String filter) {

        if (filter != null) {
            ServiceResponse.forException(new UnsupportedOperationException("No filter specified"));
        }

        List<DelegationType> types = query()
                .select(qDelegationType)
                .from(qDelegationType)
                .where(isActive(qDelegationType), visibleToClient(qDelegationType))
                .fetch();

        List<DelegationTypeView> views = types.stream()
                .map(DelegationType::buildPublicView)
                .collect(Collectors.toList());

        return ImmutableDelegationTypesView.builder().delegationTypes(views).build();
    }

    public Optional<DelegationTypeView> findByCrn(final String crn) {

        DelegationType delegationType = query()
                .select(qDelegationType)
                .from(qDelegationType)
                .where(isActive(qDelegationType), visibleToClient(qDelegationType), qDelegationType.crn.eq(crn))
                .fetchOne();

        return Optional.ofNullable(delegationType).map(DelegationType::buildPublicView);

    }

    @Transactional
    public ServiceResponse<DelegationType> create(final DelegationType delegationType) {

        delegationType.setClient(authContext.getClient());
        return save(delegationType);
    }

    @Transactional
    public ServiceResponse<DelegationType> delete(final String crn) {

        DelegationType delegationType = query()
                .select(qDelegationType)
                .from(qDelegationType)
                .where(isActive(qDelegationType), visibleToClient(qDelegationType), qDelegationType.crn.eq(crn))
                .fetchOne();

        delegationType.setDeletedAt();

        return save(delegationType);
    }

    @Transactional
    public ServiceResponse<DelegationType> update(final String crn, DelegationType params) {

        logger.debug("DelegationTypeService::update");

        DelegationType delegationType = query()
                .select(qDelegationType)
                .from(qDelegationType)
                .where(isActive(qDelegationType), visibleToClient(qDelegationType), qDelegationType.crn.eq(crn))
                .fetchOne();

        // check for template reassignment, requires both name and version
        Template templateParams = params.getTemplate();
        if (templateParams != null) {
            logger.debug("Reassigning Template: name={}, verison={}", templateParams.getName(), templateParams.getVersion());
            if (templateParams.getName() != null || templateParams.getVersion() != null ) {
                Template template = query()
                        .select(qTemplate)
                        .from(qTemplate)
                        .where(
                                isActive(qTemplate),
                                visibleToClient(qTemplate),
                                qTemplate.name.eq(templateParams.getName()),
                                qTemplate.version.eq(templateParams.getVersion()))
                        .fetchOne();

                logger.debug("Found Template: {}", template);

                if (template != null) {
                    delegationType.setTemplate(template);
                }
            }
        }

        return save(delegationType);
    }



}
