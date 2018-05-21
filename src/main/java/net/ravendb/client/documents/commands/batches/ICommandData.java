package net.ravendb.client.documents.commands.batches;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import net.ravendb.client.documents.conventions.DocumentConventions;

import java.io.IOException;

public interface ICommandData {
    String getId();

    String getName();

    String getChangeVector();

    CommandType getType();

    void serialize(JsonGenerator generator, DocumentConventions conventions) throws IOException;
}
