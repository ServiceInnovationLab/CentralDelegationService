package delegations.cds.services;


import java.text.MessageFormat;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.NotFoundException;

import com.querydsl.core.types.dsl.BooleanExpression;

import delegations.cds.models.Client;
import delegations.cds.models.QClient;


@RequestScoped
public class ClientService extends AbstractService {

    @Transactional
    public ServiceResponse<Client> create(final Client clientParams) {
    	logger.info("client::create");

        if (!authContext.getClient().getAdmin()) {
            return ServiceResponse.forException(
                    new NotAllowedException(MessageFormat.format(
                            "Only the Admin Client account can create more clients.  {0} is not authorized.",
                            authContext.getClient().getCrn())));
        }

        Client client = Client.buildFrom(clientParams);

        try {
            em.persist(client);
            return ServiceResponse.forSuccess(client);
        } catch (PersistenceException e) {
            logger.error("Failed to persist");
            return ServiceResponse.forException(e);
        }
    }

    public List<Client> findAll() {
        return em.createNamedQuery("Client.findAll", Client.class).getResultList();
    }

    public Client findByCrn(final String crn) {
        return em.createNamedQuery("Client.findByCrn", Client.class)
                .setParameter("crn", crn)
                .getSingleResult();
    }


    public ServiceResponse<Client> findByAccessKey(final String accessKey) {
        Client client = query()
                .select(QClient.client)
                .from(QClient.client)
                .where(QClient.client.accessKey.eq(accessKey))
                .fetchOne();

        logger.debug("Found client <{}> for accessKey <{}>", client, accessKey);

        if (client == null) {
            logger.error("Client for accessKey {} not exist", accessKey);
            return ServiceResponse.forException(new NotFoundException(MessageFormat.format("Client for accessKey {0} does not exist", accessKey)));
        } else if (client.getDeleteTime() != null) {
            logger.error("Client {} is deleted", client.getCrn());
            return ServiceResponse.forException(new NotFoundException(MessageFormat.format("Client {0} is deleted", client.getCrn())));
        }

        return ServiceResponse.forSuccess(client);
    }

    /**
     *  Query Helpers
     **/
     public BooleanExpression isActive() {
        return QClient.client.deleteTime.isNull();
    }




}
