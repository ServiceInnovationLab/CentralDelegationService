package delegations.cds.services;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;

import com.google.common.base.Strings;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import delegations.cds.api.DelegationRequest;
import delegations.cds.lib.CrnSchema;
import delegations.cds.lib.IdentityClient;
import delegations.cds.lib.MailClient;
import delegations.cds.lib.UmaClient;
import delegations.cds.lib.ValidationResponse;
import delegations.cds.models.Client;
import delegations.cds.models.Delegation;
import delegations.cds.models.DelegationType;
import delegations.cds.models.Email;
import delegations.cds.models.QUser;
import delegations.cds.models.Rendezvous;
import delegations.cds.models.Resource;
import delegations.cds.models.User;
import delegations.cds.models.enums.UmaResourceStatus;

public class DelegationRequestService extends AbstractService {

    @Inject
    private IdentityClient identityClient;

    @Inject
    private UmaClient umaClient;

    @Inject
    private RendezvousService rendezvousService;

    @Inject
    private MailClient mailClient;
    
    @Transactional
    public ServiceResponse<DelegationState> create(final DelegationRequest request) {

        logger.debug("Enter create");

        ServiceResponse<DelegationState> validationResponse = validateRequest(request);
        if (!validationResponse.successful()) {
        	return validationResponse;
        }

        try {
        	ServiceResponse<DelegationType> delegationTypeResponse = getDelegationType(request.getDelegationTypeCrn());
        	
        	if (!delegationTypeResponse.successful()) {
        		return delegationTypeResponse.propagate();
        	}
    		DelegationType delegationType = delegationTypeResponse.asSuccess().payload();

            logger.debug("Found delegation type: {}", delegationType);

            ServiceResponse<Resource> resourceResponse = getResource(request.getResourceCrn());
            if (!resourceResponse.successful()) {
            	return resourceResponse.propagate();
            }
            Resource resource = resourceResponse.asSuccess().payload();

            logger.debug("Found resource: {}", resource);
            // is requested resource valid for this client?

            User owner = null;
            if (request.getOwnerRcms() != null) {
                ServiceResponse<User> fetchUserResponse = fetchUserForRcms(request.getOwnerRcms());
                if (!fetchUserResponse.successful()) {
                    return fetchUserResponse.propagate();
                }
                owner = fetchUserResponse.asSuccess().payload();
            }

            if (owner != null && resource.getUser() != null && !owner.equals(resource.getUser())) {
                logger.error("Requested owner {} doesn't match resource owner {}", owner.getId(), resource.getUser().getId());
                return ServiceResponse.forException(new BadRequestException("Requested owner doesn't match resource owner"));
            }

            User delegate = null;
            if (request.getAgentRcms() != null) {
                ServiceResponse<User> fetchUserResponse = fetchUserForRcms(request.getAgentRcms());
                if (!fetchUserResponse.successful()) {
                    return fetchUserResponse.propagate();
                }
                delegate = fetchUserResponse.asSuccess().payload();
            }

            if (owner != null
                    && request.getOwnerConsent()
                    && delegate != null
                    && request.getAgentConsent()) {
                return createDelegation(request, delegationType, resource, owner, delegate);
            } else {
                return createRendezvous(request, delegationType, resource, owner, delegate);
            }

        } catch (NoResultException e) {
            logger.error("No result");
            return ServiceResponse.forException(e);
        } catch (PersistenceException e) {
            logger.error("Failed to persist");
            return ServiceResponse.forException(e);
        }

    }
    

	private ServiceResponse<DelegationState> validateRequest(final DelegationRequest request) {
		// Check that crns are valid
        ValidationResponse vrdt = CrnSchema.validateDelegationType(request.getDelegationTypeCrn());
        
        if (!vrdt.isValid()) {
            return ServiceResponse.forException(vrdt.formattedMessage());
        }
        logger.debug("DelegationType crn valid");

        ValidationResponse vrr = CrnSchema.validateResource(request.getResourceCrn());
        if (!vrr.isValid()) {
            return ServiceResponse.forException(vrr.formattedMessage());
        }
        logger.debug("Resource crn valid");

        if (request.getOwnerConsent() && Strings.isNullOrEmpty(request.getOwnerRcms())) {
            return ServiceResponse.forException("Owner consent requires an RCMS token");
        }

        if (request.getAgentConsent() && Strings.isNullOrEmpty(request.getAgentRcms())) {
            return ServiceResponse.forException("Agent consent requires an RCMS token");
        }
        
        return ServiceResponse.forSuccess(null);
	}

    private ServiceResponse<DelegationType> getDelegationType(String crn){
    	// Look up the active+visible delegation type
        DelegationType delegationType = query()
                .select(qDelegationType)
                .from(qDelegationType)
                .where(qDelegationType.client.eq(authContext.getClient()),
                        qDelegationType.deleteTime.isNull(),
                        qDelegationType.crn.eq(crn))
                .fetchOne();

        if (delegationType == null) {
            String message = MessageFormat.format("No delegation_type found for crn {0}", crn);
            logger.error(message);
            return ServiceResponse.forException(message);
        }

        return ServiceResponse.forSuccess(delegationType);
    }
    
    private ServiceResponse<Resource> getResource(String crn){
    	// Lookup the active+visible resource
        Resource resource = query()
                .select(qResource)
                .from(qResource)
                .where(
                        qResource.client.eq(authContext.getClient()),
                        qResource.deleteTime.isNull(),
                        qResource.crn.eq(crn))
                .fetchOne();

        if (resource == null) {
            String message = MessageFormat.format("No resource found for crn {0}", crn);
            logger.error(message);
            return ServiceResponse.forException(message);
        }
    
        return ServiceResponse.forSuccess(resource);
    }

    private ServiceResponse<DelegationState> createDelegation(
            final DelegationRequest request,
            final DelegationType delegationType,
            final Resource resource,
            final User owner,
            final User delegate) {

        Objects.requireNonNull(request.getOwnerRcms(), "Owner rcms must not be null");
        Objects.requireNonNull(request.getAgentRcms(), "Agent rcms must not be null");

        Resource registeredResource;
        if (resource.getOpenamId() == null) {
            resource.setUser(owner);
            ServiceResponse<Resource> registerResourceResponse = umaClient.registerResource(resource);
            if (!registerResourceResponse.successful()) {
                return registerResourceResponse.propagate();
            }
            registeredResource = em.merge(registerResourceResponse.asSuccess().payload());
        } else {
            // resource is already registered, nothing to do
            registeredResource = resource;
            // is resource owned by owner?
        }

        ServiceResponse<UmaResourceStatus> protectResourceResponse = umaClient.protectResource(registeredResource, delegate);
        if (!protectResourceResponse.successful()) {
            return protectResourceResponse.propagate();
        }

        long now = Instant.now().toEpochMilli();

        Delegation delegation = new Delegation();
        delegation.setClient(resource.getClient());
        delegation.setDelegationType(delegationType);
        delegation.setResource(registeredResource);
        delegation.setOwner(owner);
        delegation.setOwnerConsentTime(new Timestamp(now));
        delegation.setDelegate(delegate);
        delegation.setDelegateConsentTime(new Timestamp(now));

        delegation.setCrn(authContext.getClient().generateDelegationCrn());

        em.persist(delegation);

        return ServiceResponse.forSuccess(DelegationState.from(delegation));
    }

    private ServiceResponse<User> fetchUserForRcms(final String rcmsToken) {
        ServiceResponse<String> rcmsExchange = identityClient.exchangeRcmsForCdsFlt(rcmsToken);
        if (!rcmsExchange.successful()) {
            return rcmsExchange.asError().propagate();
        }

        String flt = rcmsExchange.asSuccess().payload();
        User user = query()
                .select(QUser.user)
                .from(QUser.user)
                .where(QUser.user.realmeFlt.eq(flt))
                .fetchOne();

        if (user != null) {
            logger.debug("Found user for FLT {}", flt);
            return ServiceResponse.forSuccess(user);
        } else {
            logger.debug("No user for FLT {}, creating them", flt);

            User createdUser = new User();
            createdUser.setRealmeFlt(flt);

            em.persist(createdUser);
            return ServiceResponse.forSuccess(createdUser);
        }
    }

    private ServiceResponse<DelegationState> createRendezvous(
            final DelegationRequest request,
            final DelegationType delegationType,
            final Resource resource,
            final User owner,
            final User delegate) {

        Client client = authContext.getClient();

        Rendezvous rendezvous = new Rendezvous();
        rendezvous.setClient(client);
        rendezvous.setCrn(client.generateRendezvousCrn());
        rendezvous.setOwnerEmail(request.getOwnerEmail());
        rendezvous.setDelegateEmail(request.getAgentEmail());

        Instant now = Instant.now();
        Delegation incompleteDelegation = new Delegation();
        incompleteDelegation.setClient(client);
        incompleteDelegation.setDelegationType(delegationType);
        incompleteDelegation.setResource(resource);
        incompleteDelegation.setCrn(client.generateDelegationCrn());
        incompleteDelegation.setRendezvous(rendezvous);

        /*
         * Here's the different situations that the following if-else blocks are meant to represent:
         *
         *                                  consent provided?
         *
         *                       |       yes        |       no
         *                 ------+------------------+------------------
         *                       | set owner        | set owner
         *                  yes  |                  | set code
         *                       | set consent time | send email
         * rcms provided?  ------+------------------+------------------
         *                       |                  | set code
         *                  no   | error            |
         *                       |                  | send email
         *                 ------+------------------+------------------
         */

        if (!request.getOwnerConsent()) {
            rendezvous.setOwnerCode(UUID.randomUUID().toString());
            incompleteDelegation.setOwner(owner);
        } else if (owner != null) {
            incompleteDelegation.setOwner(owner);
            incompleteDelegation.setOwnerConsentTime(new Timestamp(now.toEpochMilli()));
        } else {
            return ServiceResponse.forException("Owner consent provided but owner not identified");
        }

        if (!request.getAgentConsent()) {
            rendezvous.setDelegateCode(UUID.randomUUID().toString());
            incompleteDelegation.setDelegate(delegate);
        } else if (delegate != null) {
            incompleteDelegation.setDelegate(delegate);
            incompleteDelegation.setDelegateConsentTime(new Timestamp(now.toEpochMilli()));
        } else {
            return ServiceResponse.forException("Delegate consent provided but delegate not identified");
        }

        em.persist(rendezvous);
        em.persist(incompleteDelegation);
        em.flush();

        // figure out emails after the delegation is set up
        if (!request.getOwnerConsent() || !request.getAgentConsent()) {
            Email email = rendezvousService.generateEmail(incompleteDelegation);
            mailClient.send(email);
        }

        return ServiceResponse.forSuccess(DelegationState.from(rendezvous));
    }

    @Transactional
    public ServiceResponse<Void> accept(final Integer delegationRequestId, final String rcms, final String consentToken) {
        // Lookup Delegation

        return ServiceResponse.forSuccess();
    }

    /**
     * Query Helpers
     **/
    public BooleanExpression visibleToClient() {
        if (authContext.getClient() == null) {
            return Expressions.TRUE;
        }
        return qDelegationType.client.eq(authContext.getClient());
    }

    public BooleanExpression isActive() {
        return qDelegationType.deleteTime.isNull();
    }

}
