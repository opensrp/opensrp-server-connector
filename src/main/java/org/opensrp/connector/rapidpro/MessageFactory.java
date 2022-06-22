package org.opensrp.connector.rapidpro;

public abstract class MessageFactory {

    public static MessageFactory getMessageFactory(MessageType type) {
        MessageFactory messageFactory = null;
        if (type == MessageType.ANNOUNCEMENT) {
            messageFactory = new AnnouncementMessage();
        } else if (type == MessageType.REMINDER) {
            messageFactory = new ReminderMessage();
        }
        return messageFactory;

    }

    public abstract Message getClientType(ClientType type);

}
