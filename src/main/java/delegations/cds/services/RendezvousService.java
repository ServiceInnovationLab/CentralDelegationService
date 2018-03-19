package delegations.cds.services;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;

import delegations.cds.lib.IdentityClient;
import delegations.cds.lib.UmaClient;
import delegations.cds.models.Delegation;
import delegations.cds.models.DelegationType;
import delegations.cds.models.Email;
import delegations.cds.models.EmailAddress;
import delegations.cds.models.ImmutableEmail;
import delegations.cds.models.ImmutableEmailAddress;
import delegations.cds.models.QRendezvous;
import delegations.cds.models.Rendezvous;
import delegations.cds.models.Resource;
import delegations.cds.models.Template;
import delegations.cds.models.User;
import delegations.cds.models.enums.UmaResourceStatus;
import delegations.cds.util.Setting;
import delegations.cds.util.Settings;
import delegations.cds.views.ImmutableRendezvousesView;
import delegations.cds.views.RendezvousView;
import delegations.cds.views.RendezvousesView;

@RequestScoped
public class RendezvousService extends AbstractService {

    private static final String EMAIL_CONTEXT_VARIABLE_RESOURCE_CRN = "resource_crn";

	private static final String EMAIL_CONTEXT_VARIABLE_DELEGATE_EMAIL = "delegate_email";

	private static final String EMAIL_CONTEXT_VARIABLE_OWNER_EMAIL = "owner_email";

	@Inject
    private UmaClient umaClient;

    @Inject
    private TemplateService templateService;

    @Inject
    private IdentityClient identityClient;

    private final TemplateEngine engine;

    public RendezvousService() {
        engine = new TemplateEngine();
    }

    public RendezvousesView findAll(final String filter) {
        QRendezvous rendezvous = QRendezvous.rendezvous;

        if (filter != null) {
            ServiceResponse.forException(new UnsupportedOperationException("No filter specified"));
        }

        List<Rendezvous> rendezvouses = query()
                .select(rendezvous)
                .from(rendezvous)
                .where(isActive(), visibleToClient())
                .fetch();

        List<RendezvousView> rviews = rendezvouses.stream()
                .map(Rendezvous::buildPublicView)
                .collect(Collectors.toList());

        return ImmutableRendezvousesView.builder().rendezvouses(rviews).build();
    }

    public Rendezvous findByAccessCode(final String accessCode) {

        return query()
                .select(qRendezvous)
                .from(qRendezvous)
                .where(isActive(), visibleToClient(), validOwnerCode(accessCode).or(validDelegateCode(accessCode)))
                .fetchOne();
    }

    @Transactional
    public ServiceResponse<DelegationState> accept(final String inviteCode, final String rcmsToken) {

        if (inviteCode == null) {
            return ServiceResponse.forException("Invite code is required");
        }

        if (rcmsToken == null) {
            return ServiceResponse.forException("Rcms token is required");
        }

        Delegation delegation = query()
                .select(qDelegation)
                .from(qDelegation)
                .join(qDelegation.rendezvous, qRendezvous)
                .where(
                        isActive(qDelegation),
                        isActive(qRendezvous),
                        visibleToClient(qDelegation),
                        qRendezvous.ownerCode.eq(inviteCode).and(qRendezvous.ownerCodeConsumed.eq(false)).or(qRendezvous.delegateCode.eq(inviteCode).and(qRendezvous.delegateCodeConsumed.eq(false))))
                .fetchOne();

        if (delegation == null) {
            return ServiceResponse.forException(new NotFoundException(MessageFormat.format("No matching rendezvous for code {0}", inviteCode)));
        }

        boolean isUserDelegate = inviteCode.equals(delegation.getRendezvous().getDelegateCode());

        ServiceResponse<String> rcmsExchangeResponse = identityClient.exchangeRcmsForCdsFlt(rcmsToken);
        if (!rcmsExchangeResponse.successful()) {
            return rcmsExchangeResponse.propagate();
        }
        String flt = rcmsExchangeResponse.asSuccess().payload();

        User user = query()
                .select(qUser)
                .from(qUser)
                .where(
                        isActive(qUser),
                        qUser.realmeFlt.eq(flt))
                .fetchOne();

        if (user == null) {
            logger.info("No matching user for rcms token, creating");
            user = new User();
            user.setRealmeFlt(flt);
            em.persist(user);
        }

        Rendezvous rendezvous = delegation.getRendezvous();

        Instant now = Instant.now();
        if (isUserDelegate) {
            delegation.setDelegate(user);
            delegation.setDelegateConsentTime(new Timestamp(now.toEpochMilli()));
            rendezvous.setDelegateCodeConsumed(true);
        } else {
            delegation.setOwner(user);
            delegation.setOwnerConsentTime(new Timestamp(now.toEpochMilli()));
            rendezvous.setOwnerCodeConsumed(true);
        }

        em.merge(rendezvous);
        em.merge(delegation);

        if (delegation.hasBothConsents()) {
            Resource registeredResource = delegation.getResource();

            if (registeredResource.getOpenamId() == null) {

                if (registeredResource.getUser() == null) {
                    registeredResource.setUser(delegation.getOwner());
                }

                ServiceResponse<Resource> registerResourceResponse = umaClient.registerResource(registeredResource);
                if (!registerResourceResponse.successful()) {
                    return registerResourceResponse.propagate();
                }
                registeredResource = em.merge(registerResourceResponse.asSuccess().payload());
            }

            ServiceResponse<UmaResourceStatus> protectResourceResponse = umaClient.protectResource(registeredResource, delegation.getDelegate());
            if (!protectResourceResponse.successful()) {
                return protectResourceResponse.propagate();
            }

            return ServiceResponse.forSuccess(DelegationState.from(delegation));
        } else {
            return ServiceResponse.forSuccess(DelegationState.from(rendezvous));
        }
    }

    /**
     * Lookup the Template from the linked Delegation, and render an Email value object
     *
     * @param rendezvous
     * @return
     */
    public Email generateEmail(final Delegation delegation) {

        Rendezvous rendezvous = delegation.getRendezvous();

        DelegationType type = delegation.getDelegationType();

        Template template = type.getTemplate();

        if (template == null) {
            template = templateService.defaultTemplateForClient();
        }

        Context context = new Context();
        context.setVariable(EMAIL_CONTEXT_VARIABLE_OWNER_EMAIL, delegation.getRendezvous().getOwnerEmail());
        context.setVariable(EMAIL_CONTEXT_VARIABLE_DELEGATE_EMAIL, delegation.getRendezvous().getDelegateEmail());
        context.setVariable(EMAIL_CONTEXT_VARIABLE_RESOURCE_CRN, delegation.getResource().getCrn());

        String processedContent = engine.process(template.getContent(), context);

        EmailAddress sender = ImmutableEmailAddress.builder()
        		.email(Settings.getSetting(Setting.RENDEZVOUS_EMAIL_ADDRESS_SENDER_FIELD))
        		.name(Settings.getSetting(Setting.RENDEZVOUS_EMAIL_SENDER_NAME))
        		.build();
        EmailAddress from = ImmutableEmailAddress.builder()
        		.email(Settings.getSetting(Setting.RENDEZVOUS_EMAIL_ADDRESS_FROM_FIELD))
        		.name(rendezvous.getClient().getCrn())
        		.build();

        List<EmailAddress> recipients = Lists.newArrayList();

        if (delegation.getRendezvous().getOwnerEmail() != null) {
            recipients.add(ImmutableEmailAddress.builder()
                    .email(delegation.getRendezvous().getOwnerEmail())
                    .name(Settings.getSetting(Setting.RENDEZVOUS_EMAIL_OWNER_NAME))
                    .build());
        }

        if (delegation.getRendezvous().getDelegateEmail() != null) {
            recipients.add(ImmutableEmailAddress.builder()
                    .email(delegation.getRendezvous().getDelegateEmail())
                    .name(Settings.getSetting(Setting.RENDEZVOUS_EMAIL_DELEGATE_NAME))
                    .build());
        }

        return ImmutableEmail.builder()
                .subject(Settings.getSetting(Setting.RENDEZVOUS_EMAIL_SUBJECT))
                .recipientAddresses(recipients)
                .senderAddress(sender)
                .fromAddress(from)
                .content(processedContent)
                .build();
    }

    /**
     * Query Helpers
     **/
    public BooleanExpression visibleToClient() {
        if (authContext.getClient() == null) {
            return Expressions.TRUE;
        }
        return QRendezvous.rendezvous.client.eq(authContext.getClient());
    }

    public BooleanExpression isActive() {
        return QRendezvous.rendezvous.deleteTime.isNull();
    }

    public BooleanExpression validOwnerCode(final String code) {
        return qRendezvous.ownerCode.eq(code).and(qRendezvous.ownerCodeConsumed.eq(false));
    }

    public BooleanExpression validDelegateCode(final String code) {
        return qRendezvous.delegateCode.eq(code).and(qRendezvous.delegateCodeConsumed.eq(false));
    }

    @Override
    public JPAQuery<?> query() {
        return new JPAQuery<>(em);
    }

}
