package net.ravendb.client.test.client;

import net.ravendb.client.Constants;
import net.ravendb.client.RemoteTestBase;
import net.ravendb.client.documents.DocumentStore;
import net.ravendb.client.documents.IDocumentStore;
import net.ravendb.client.documents.commands.GetRevisionsBinEntryCommand;
import net.ravendb.client.documents.session.IDocumentSession;
import net.ravendb.client.infrastructure.entities.User;
import net.ravendb.client.json.JsonArrayResult;
import net.ravendb.client.json.MetadataAsDictionary;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RevisionsTest extends RemoteTestBase {
    @Test
    public void revisions() throws Exception {
        try (DocumentStore store = getDocumentStore()) {
            setupRevisions(store, false, 4);


            for (int i = 0; i < 4; i++) {
                try (IDocumentSession session = store.openSession()) {
                    User user = new User();
                    user.setName("user" + (i + 1));
                    session.store(user, "users/1");
                    session.saveChanges();
                }
            }

            try (IDocumentSession session = store.openSession()) {
                List<User> allRevisions = session.advanced().revisions().getFor(User.class, "users/1");
                assertThat(allRevisions)
                        .hasSize(4);
                assertThat(allRevisions.stream().map(x -> x.getName()).collect(Collectors.toList()))
                        .containsSequence("user4", "user3", "user2" , "user1");

                List<User> revisionsSkipFirst = session.advanced().revisions().getFor(User.class, "users/1", 1);
                assertThat(revisionsSkipFirst)
                        .hasSize(3);
                assertThat(revisionsSkipFirst.stream().map(x -> x.getName()).collect(Collectors.toList()))
                        .containsSequence("user3", "user2" , "user1");

                List<User> revisionsSkipFirstTakeTwo = session.advanced().revisions().getFor(User.class, "users/1", 1, 2);
                assertThat(revisionsSkipFirstTakeTwo)
                        .hasSize(2);
                assertThat(revisionsSkipFirstTakeTwo.stream().map(x -> x.getName()).collect(Collectors.toList()))
                        .containsSequence("user3", "user2" );

                List<MetadataAsDictionary> allMetadata = session.advanced().revisions().getMetadataFor("users/1");
                assertThat(allMetadata)
                        .hasSize(4);

                List<MetadataAsDictionary> metadataSkipFirst = session.advanced().revisions().getMetadataFor("users/1", 1);
                assertThat(metadataSkipFirst)
                        .hasSize(3);

                List<MetadataAsDictionary> metadataSkipFirstTakeTwo = session.advanced().revisions().getMetadataFor("users/1", 1, 2);
                assertThat(metadataSkipFirstTakeTwo)
                        .hasSize(2);


                User user = session.advanced().revisions().get(User.class, (String) metadataSkipFirst.get(0).get(Constants.Documents.Metadata.CHANGE_VECTOR));
                assertThat(user.getName())
                        .isEqualTo("user3");
            }
        }
    }

    @Test
    public void canListRevisionsBin() throws Exception {
        try (IDocumentStore store = getDocumentStore()) {
            setupRevisions(store, false, 4);

            try (IDocumentSession session = store.openSession()) {
                User user = new User();
                user.setName("user1");
                session.store(user, "users/1");
                session.saveChanges();
            }

            try (IDocumentSession session = store.openSession()) {
                session.delete("users/1");
                session.saveChanges();
            }

            GetRevisionsBinEntryCommand revisionsBinEntryCommand = new GetRevisionsBinEntryCommand(Long.MAX_VALUE, 20);
            store.getRequestExecutor().execute(revisionsBinEntryCommand);

            JsonArrayResult result = revisionsBinEntryCommand.getResult();
            assertThat(result.getResults())
                    .hasSize(1);

            assertThat(result.getResults().get(0).get("@metadata").get("@id").asText())
                    .isEqualTo("users/1");
        }
    }
}
