package delegations.cds.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.ForbiddenException;

import com.querydsl.core.QueryResults;

import delegations.cds.api.support.ApiConstants;
import delegations.cds.lib.IdentityClient;
import delegations.cds.lib.UmaClient;
import delegations.cds.models.Delegation;
import delegations.cds.models.Resource;
import delegations.cds.models.User;
import delegations.cds.views.ImmutableResourceAccessView;
import delegations.cds.views.ImmutableResourcesView;
import delegations.cds.views.ResourceAccessView;
import delegations.cds.views.ResourceView;
import delegations.cds.views.ResourcesView;
import io.opentracing.ActiveSpan;

@RequestScoped
public class ResourceService extends AbstractService {

    private IdentityClient identityClient;
    private UmaClient umaClient;

    @Inject
    public ResourceService(final IdentityClient identityClient, final UmaClient umaClient) {
        this.identityClient = identityClient;
        this.umaClient = umaClient;
    }

    ResourceService() {
        // no-arg constructor so cdi can proxy
    }

    @Transactional
    public ServiceResponse<Resource> create(final Resource resource) {

        resource.setClient(authContext.getClient());
        return save(resource);
    }

    @Transactional
    public ServiceResponse<Resource> delete(final String crn) {

        Resource resource = query()
                .select(qResource)
                .from(qResource)
                .where(isActive(qResource), visibleToClient(qResource), qResource.crn.eq(crn))
                .fetchOne();

        resource.setDeletedAt();

        return save(resource);

    }

    public ServiceResponse<ResourcesView> findAll(final String filter) {

        try (ActiveSpan span = tracer.buildSpan("services/ResourceService/findAll").startActive()) {
            span.setTag("filter", filter);

            if (!filter.equals(ApiConstants.NONE)) {
                span.setTag("error.kind", "UnsupportedOperationException");
                return ServiceResponse.forException(new UnsupportedOperationException("TODO"));
            }

            span.log("query Resource");
            List<Resource> resources = query()
                    .select(qResource)
                    .from(qResource)
                    .where(isActive(qResource), visibleToClient(qResource))
                    .fetch();


            span.log("convert ResourceView");
            List<ResourceView> rviews = resources.stream()
                    .map(Resource::buildPublicView)
                    .collect(Collectors.toList());

            ResourcesView view = ImmutableResourcesView.builder().resources(rviews).build();

            return ServiceResponse.forSuccess(view);
        }
    }

    public ResourceView findByCrn(final String crn) {

        Resource result = query()
                .select(qResource)
                .from(qResource)
                .where(isActive(qResource), visibleToClient(qResource), qResource.crn.eq(crn))
                .fetchOne();


        return result.buildPublicView();

    }

    public ServiceResponse<ResourceAccessView> checkAccess(final String crn, final String requestorRcms) {

        Resource resource = query().select(qResource).from(qResource)
                .where(
                        isActive(qResource),
                        visibleToClient(qResource),
                        qResource.crn.eq(crn))
                .fetchOne();

        String requestorCdsFlt = identityClient.exchangeRcmsForCdsFlt(requestorRcms)
                .asSuccess()
                .payload();

        User requestor = query().select(qUser).from(qUser)
                .where(
                        isActive(qUser),
                        qUser.realmeFlt.eq(requestorCdsFlt))
                .fetchOne();

        ServiceResponse<Boolean> accessResponse = umaClient.checkAccessToResource(resource, requestor);
        if (!accessResponse.successful()) {
            return accessResponse.propagate();
        }

        if (!accessResponse.asSuccess().payload()) {
            return ServiceResponse.forException(new ForbiddenException("Access denied"));
        }

        // look up delegation and apply any further processing
        QueryResults<Delegation> results = query()
                .select(qDelegation)
                .from(qDelegation)
                .where(
                        isActive(qDelegation),
                        visibleToClient(qDelegation),
                        qDelegation.resource.eq(resource),
                        qDelegation.delegate.realmeFlt.eq(requestorCdsFlt).or(qDelegation.owner.realmeFlt.eq(requestorCdsFlt)))
                .fetchResults();

        if (results.isEmpty()) {
            return ServiceResponse.forException(new IllegalStateException("OpenAM allowed access, but we don't have a delegation record"));
        }

        // ... not that we have any further processing at this point in time

        ResourceAccessView view = ImmutableResourceAccessView.builder()
                .resource(resource.buildPublicView())
                .permitted(true)
                .build();

        return ServiceResponse.forSuccess(view);
    }

}
