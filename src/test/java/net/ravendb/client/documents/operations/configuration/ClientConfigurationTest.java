package net.ravendb.client.documents.operations.configuration;

import net.ravendb.client.RemoteTestBase;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.http.ReadBalanceBehavior;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientConfigurationTest extends RemoteTestBase {

    @Test
    public void canHandleNoConfiguration() throws Exception {
        try (IDocumentStore store = getDocumentStore()) {
            GetClientConfigurationOperation operation = new GetClientConfigurationOperation();
            GetClientConfigurationOperation.Result result = store.maintenance().send(operation);

            assertThat(result.getConfiguration())
                    .isNull();

            assertThat(result.getEtag())
                    .isNotNull();
        }
    }

    @Test
    public void canSaveAndReadClientConfiguration() throws Exception {
        try (IDocumentStore store = getDocumentStore()) {

            ClientConfiguration configurationToSave = new ClientConfiguration();
            configurationToSave.setEtag(123L);
            configurationToSave.setMaxNumberOfRequestsPerSession(80);
            configurationToSave.setReadBalanceBehavior(ReadBalanceBehavior.FASTEST_NODE);
            configurationToSave.setDisabled(true);

            PutClientConfigurationOperation saveOperation = new PutClientConfigurationOperation(configurationToSave);

            store.maintenance().send(saveOperation);

            GetClientConfigurationOperation operation = new GetClientConfigurationOperation();
            GetClientConfigurationOperation.Result result = store.maintenance().send(operation);

            assertThat(result.getEtag())
                    .isNotNull();

            ClientConfiguration newConfiguration = result.getConfiguration();

            assertThat(newConfiguration)
                    .isNotNull();

            assertThat(newConfiguration.getEtag())
                    .isGreaterThan(configurationToSave.getEtag());

            assertThat(newConfiguration.isDisabled())
                    .isTrue();

            assertThat(newConfiguration.getMaxNumberOfRequestsPerSession())
                    .isEqualTo(80);

            assertThat(newConfiguration.getReadBalanceBehavior())
                    .isEqualTo(ReadBalanceBehavior.FASTEST_NODE);
        }
    }
}
