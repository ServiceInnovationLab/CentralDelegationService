package delegations.cds.lib;

import com.google.common.collect.Lists;

import java.util.List;

public class ValidationResponse {

    private List<String> messages;

    public ValidationResponse() {
       messages = Lists.newArrayList();
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public boolean isValid() {
        return messages.isEmpty();
    }

    public String formattedMessage() {
        return String.join("\n", messages);
    }
}
